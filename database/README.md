# Base de datos — PostgreSQL 14+

| Carpeta | Contenido |
|---|---|
| `roles/00_roles.sql` | Roles de mínimo privilegio: `gd_migrator` (DDL) y `gd_app` (sin DELETE ni DDL) |
| `migrations/` | Migraciones Flyway versionadas. Son la única fuente del esquema |
| `consultas/consultas_preparadas.sql` | Consultas de referencia con parámetros (`PREPARE`) |

## Modelo

- `personas` (superclase) → `clientes` y `admin` (subclases, misma PK `id_persona`)
- `deudas`: deudas de un cliente registradas por un admin
- `pagos`: libro de abonos **inmutable** (sin UPDATE, DELETE ni TRUNCATE)
- `total_dinero`: acumulado por cliente

## Reglas

1. Dinero siempre `NUMERIC(14,2)`. Nunca `REAL`, `DOUBLE PRECISION` ni `FLOAT`.
2. Sin `DELETE`: un trigger lo bloquea. Se usa `activo` (1 = activo, 0 = inactivo).
3. Los cambios de esquema van como nuevas migraciones `V3__...sql`. Nunca se edita una migración ya aplicada.

## Uso manual

```bash
psql -U postgres -v app_password='***' -v migrator_password='***' -f database/roles/00_roles.sql
# Flyway aplica migrations/ al arrancar la API (o con: mvn flyway:migrate)
```
