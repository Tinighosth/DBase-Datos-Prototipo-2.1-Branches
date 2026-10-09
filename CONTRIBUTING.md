# Guía de contribución — GitHub Flow

`main` siempre está estable y desplegable. Todo cambio entra por Pull Request.

## Flujo

1. **Crear rama desde `main`** con nombre descriptivo:
   `feature/abonos-parciales`, `fix/redondeo-total`, `docs/readme`.
   ```bash
   git switch main && git pull
   git switch -c feature/nombre-corto
   ```
2. **Commits pequeños y claros** (convención sugerida: `feat:`, `fix:`, `test:`, `docs:`, `refactor:`).
3. **Subir la rama y abrir un Pull Request** hacia `main` en cuanto haya algo que discutir (puede ser borrador).
4. **CI automático**: el flujo `CI` ejecuta `mvn verify` (unitarias + integración). Debe quedar en verde.
5. **Revisión**: al menos una aprobación. Se atienden los comentarios con nuevos commits.
6. **Merge a `main`** (squash recomendado) y **borrar la rama**.
7. **Desplegar desde `main`**. Un fallo se corrige con otro PR, no con commits directos.

## Reglas de protección recomendadas para `main`

En *Settings → Branches → Branch protection rules*:

- Require a pull request before merging (1 aprobación mínima)
- Require status checks to pass: `build-test`
- Require branches to be up to date before merging
- Do not allow bypassing the above settings
- Block force pushes

## Estándares del proyecto

- Dinero: `BigDecimal` en Java y `NUMERIC(14,2)` en PostgreSQL. Nunca `float` ni `double`.
- Sin `DELETE`: se desactiva con `activo = 0`.
- Los pagos son inmutables: se agregan, nunca se modifican.
- Cambios de esquema: nueva migración en `database/migrations/` (`V3__descripcion.sql`).
- Secretos solo por variables de entorno.
- Toda lógica nueva lleva pruebas (JUnit 5 + Mockito para unitarias; `*IT` para integración).
