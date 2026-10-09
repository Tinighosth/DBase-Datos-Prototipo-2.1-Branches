# Gestión de Deudas — API REST

Java 21 · Spring Boot 3 · PostgreSQL · Hibernate (JPA) · Flyway · JUnit 5 · Mockito · Testcontainers

API para administrar **clientes**, **administradores**, **deudas** y **abonos**.

## Arquitectura (Clean Architecture)

La dependencia apunta siempre hacia adentro: `presentacion → aplicacion → dominio ← infraestructura`.

```
src/main/java/com/gestiondeudas
├── dominio/           Reglas del negocio. Java puro, sin Spring ni JPA
│   ├── modelo/        Dinero, Persona (Cliente, Admin), Deuda, Pago, TotalCliente (inmutables)
│   ├── repositorio/   Puertos (interfaces) que el dominio necesita
│   └── excepcion/
├── aplicacion/        Casos de uso
│   ├── servicio/      ClienteService, AdminService, DeudaService, PagoService, TotalService
│   └── puerto/        CodificadorClave
├── infraestructura/   Detalles técnicos
│   ├── persistencia/  Entidades JPA, Spring Data y adaptadores que implementan los puertos
│   ├── seguridad/     Spring Security, BCrypt, rate limiting
│   └── config/
└── presentacion/      API REST
    ├── controlador/   Controladores
    ├── dto/           Entradas validadas y salidas verificadas
    └── error/         Errores RFC 7807 sin filtrar detalles internos
database/              Migraciones Flyway, roles de mínimo privilegio, consultas de referencia
```

**SOLID**: *S* cada servicio atiende un solo caso de uso · *O* se agregan adaptadores sin tocar el dominio ·
*L* los adaptadores cumplen el contrato de sus puertos · *I* un puerto por agregado, pequeño y específico ·
*D* los servicios dependen de interfaces del dominio (`ClienteRepository`, `CodificadorClave`), no de JPA ni BCrypt.

## Modelo de datos

`personas` (superclase) → `clientes` y `admin` · `deudas` · `pagos` (libro inmutable) · `total_dinero`.
Detalle en [`database/README.md`](database/README.md).

## Seguridad (mapeo OWASP API Security Top 10 / ASVS)

| Riesgo | Medida aplicada |
|---|---|
| Inyección SQL | Hibernate con parámetros enlazados; consultas con `@Param`, sin concatenar texto |
| Autenticación rota | HTTP Basic sobre TLS, claves con BCrypt (cost 12), mensaje de error genérico, solo admins activos |
| Autorización a nivel de función | Roles y niveles: crear o desactivar admins exige nivel TOTAL; el resto, `denyAll` |
| Mass assignment | DTOs de entrada, `fail-on-unknown-properties`, entidades nunca expuestas |
| Exposición excesiva de datos | DTOs de salida validados, documento enmascarado, sin hashes ni claves |
| Consumo sin límites | Rate limiting por IP (Bucket4j), más estricto para escrituras, paginación con tope |
| Configuración insegura | Cabeceras de seguridad, errores sin trazas, secretos por variables de entorno |
| Integridad financiera | `BigDecimal`/`NUMERIC(14,2)`, sin `float`/`double`, montos estrictos (sin redondeo silencioso) |

**Inmutabilidad y transacciones**
- Records inmutables en el dominio: `Deuda.aplicarAbono()` devuelve una nueva deuda.
- `pagos` es un libro de solo inserción: `@Immutable` en Hibernate y trigger en PostgreSQL.
- `PagoService.registrar` es **atómico** (deuda + pago + total en una transacción), con `SELECT … FOR UPDATE`
  contra abonos simultáneos y **clave de idempotencia** contra cobros duplicados.
- Sin `DELETE`: trigger de bloqueo y rol `gd_app` sin permiso DELETE.

**Alcance**: esto es una base sólida, no una certificación. Antes de producción faltan TLS en el proxy,
gestor de secretos, MFA/JWT si hay clientes externos, auditoría centralizada y pruebas de penetración.
No procesa datos de tarjetas, por lo que PCI DSS no aplica a este alcance.

## Ejecutar

```bash
cp .env.example .env               # complete los valores
docker compose --env-file .env up -d
export $(grep -v '^#' .env | xargs)
mvn spring-boot:run
```

Primer inicio: crea el administrador desde `ADMIN_BOOTSTRAP_USER` / `ADMIN_BOOTSTRAP_PASSWORD`.
Para producción siga `database/roles/00_roles.sql` y use `SPRING_PROFILES_ACTIVE=prod`.

## Endpoints (`/api/v1`, todos requieren autenticación)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/clientes` | Crear cliente |
| GET | `/clientes?pagina=0&tamano=20` | Listar activos |
| GET | `/clientes/{id}` | Obtener |
| PATCH | `/clientes/{id}/estado` | `{"activo":0}` desactiva, `{"activo":1}` reactiva |
| POST | `/admins` · PATCH `/admins/{id}/estado` | Solo nivel TOTAL |
| POST | `/deudas` | Crear deuda |
| GET | `/clientes/{id}/deudas` | Deudas activas del cliente |
| PATCH | `/deudas/{id}/estado` | Activar o desactivar |
| POST | `/deudas/{id}/pagos` | Abonar. Requiere encabezado `Idempotency-Key` |
| GET | `/deudas/{id}/pagos` | Historial de abonos |
| GET | `/clientes/{id}/total` | Total de dinero del cliente |

Ejemplo de abono:

```bash
curl -u admin:CLAVE -X POST http://localhost:8080/api/v1/deudas/1/pagos \
  -H 'Content-Type: application/json' \
  -H 'Idempotency-Key: abono-0000000000001' \
  -d '{"monto": 200000.00}'
```

Los montos salen como texto (`"300000.00"`) para que ningún cliente los convierta a `float`.

## Pruebas

```bash
mvn test       # unitarias: JUnit 5 + Mockito (sin base de datos)
mvn verify     # unitarias + integración (*IT) con PostgreSQL real en Testcontainers (requiere Docker)
```

## Ramas

`bash scripts/publicar-ramas.sh https://github.com/USUARIO/gestion-deudas.git` sube el proyecto en ramas encadenadas
(cada una nace de la anterior y el proyecto compila en cada paso). Un Pull Request por rama, en este orden:

| # | Rama | Carpeta |
|---|---|---|
| 0 | `main` | `pom.xml`, configuración, `.github/`, `scripts/` |
| 1 | `feature/base-de-datos` | `database/` |
| 2 | `feature/dominio` | `dominio/` |
| 3 | `feature/aplicacion` | `aplicacion/` |
| 4 | `feature/persistencia` | `infraestructura/persistencia/` |
| 5 | `feature/seguridad` | `infraestructura/seguridad/` |
| 6 | `feature/api-rest` | `presentacion/` |
| 7 | `feature/pruebas-integracion` | `src/test/.../integracion/` |

Mergear en orden con **Create a merge commit** (sin squash).

## Flujo de trabajo

GitHub Flow: ver [`CONTRIBUTING.md`](CONTRIBUTING.md). CI en `.github/workflows/ci.yml`.
