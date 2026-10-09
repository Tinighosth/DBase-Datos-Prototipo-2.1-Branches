#!/usr/bin/env bash
# Publica el proyecto en GitHub dividido en ramas encadenadas (una por parte).
# Uso:  bash scripts/publicar-ramas.sh https://github.com/USUARIO/gestion-deudas.git
# Requiere: git configurado (user.name y user.email). Opcional: GitHub CLI (gh) para abrir los PR.
set -euo pipefail

REMOTO="${1:-}"
[ -n "$REMOTO" ] || { echo "Uso: bash scripts/publicar-ramas.sh <URL-del-repositorio-vacio>"; exit 1; }
if ! git config user.name >/dev/null || ! git config user.email >/dev/null; then
  echo "Configure primero: git config --global user.name \"Su Nombre\" ; git config --global user.email \"correo@ejemplo.com\""
  exit 1
fi

# Trabaja en una copia temporal: su carpeta original no se toca (ni se sube su .env)
ORIGEN="$(cd "$(dirname "$0")/.." && pwd)"
TMP="$(mktemp -d)"
cp -R "$ORIGEN/." "$TMP/"
rm -rf "$TMP/.git" "$TMP/target" "$TMP/.env"
cd "$TMP"

J=src/main/java/com/gestiondeudas
T=src/test/java/com/gestiondeudas
RAMAS=()
TITULOS=()

git init -q -b main
git add -- pom.xml .gitignore .env.example docker-compose.yml README.md CONTRIBUTING.md \
  .github scripts "$J/GestionDeudasApplication.java" \
  src/main/resources/application.yml src/main/resources/application-prod.yml
git commit -q -m "chore: estructura base, configuración y CI"
echo "main  <- base"

# Cada rama nace de la anterior (ramas encadenadas): el proyecto compila en cada paso.
rama() {  # rama <nombre> <mensaje> <rutas...>
  local nombre="$1" mensaje="$2"; shift 2
  git switch -q -c "$nombre"
  git add -- "$@"
  git commit -q -m "$mensaje"
  RAMAS+=("$nombre"); TITULOS+=("$mensaje")
  echo "$nombre"
}

rama feature/base-de-datos "feat(db): esquema PostgreSQL, migraciones Flyway y roles" database
rama feature/dominio "feat(dominio): modelo inmutable, Dinero y puertos con pruebas" \
  "$J/dominio" "$T/dominio"
rama feature/aplicacion "feat(aplicacion): servicios y casos de uso con pruebas Mockito" \
  "$J/aplicacion" "$T/aplicacion"
rama feature/persistencia "feat(persistencia): entidades JPA, repositorios y adaptadores" \
  "$J/infraestructura/persistencia" "$J/infraestructura/config/RelojConfig.java"
rama feature/seguridad "feat(seguridad): Spring Security, BCrypt y rate limiting" \
  "$J/infraestructura/seguridad" "$J/infraestructura/config/AdminInicialRunner.java"
rama feature/api-rest "feat(api): controladores, DTOs validados y manejo de errores" \
  "$J/presentacion"
rama feature/pruebas-integracion "test(integracion): pruebas con PostgreSQL real (Testcontainers)" \
  "$T/integracion" src/test/resources

git remote add origin "$REMOTO"
git push -u origin main "${RAMAS[@]}"

if command -v gh >/dev/null 2>&1; then
  base=main
  for i in "${!RAMAS[@]}"; do
    gh pr create --base "$base" --head "${RAMAS[$i]}" --title "${TITULOS[$i]}" \
      --body "Parte del proyecto. Mergear en orden con 'Create a merge commit' (sin squash)." \
      || { echo "No se pudo abrir el PR con gh (¿gh auth login?). Ábralos desde GitHub."; break; }
    base="${RAMAS[$i]}"
  done
else
  echo
  echo "Abra un Pull Request por rama, EN ORDEN. Base de cada uno = la rama anterior:"
  base=main
  for r in "${RAMAS[@]}"; do echo "  $r  ->  base: $base"; base="$r"; done
fi
echo
echo "Listo. Mergee en orden con 'Create a merge commit' (no squash). Copia temporal: $TMP"
