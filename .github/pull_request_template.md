## ¿Qué cambia y por qué?

<!-- Una o dos frases. Enlace al issue: Closes #123 -->

## Lista de verificación

- [ ] La rama sale de `main` y tiene un nombre descriptivo (`feature/...`, `fix/...`)
- [ ] `mvn verify` pasa en local
- [ ] Hay pruebas nuevas o actualizadas
- [ ] Dinero solo con `BigDecimal` / `NUMERIC` (nada de `float` ni `double`)
- [ ] Sin `DELETE`: borrado lógico con `activo` 1/0
- [ ] Consultas parametrizadas, sin concatenar texto en SQL
- [ ] Los cambios de esquema van en una migración nueva `V__` (no se edita una ya aplicada)
- [ ] No se agregaron secretos al repositorio
