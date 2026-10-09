-- =====================================================================
-- 00_roles.sql  |  Mínimo privilegio (ejecutar UNA vez como superusuario)
--
--   psql -U postgres \
--        -v app_password='...' -v migrator_password='...' \
--        -f database/roles/00_roles.sql
--
-- gd_migrator : dueño del esquema, lo usa solo Flyway (DDL).
-- gd_app      : lo usa la API. SELECT / INSERT / UPDATE. Sin DELETE, sin DDL.
-- =====================================================================
CREATE ROLE gd_migrator LOGIN PASSWORD :'migrator_password';
CREATE ROLE gd_app      LOGIN PASSWORD :'app_password';

CREATE DATABASE gestion_deudas OWNER gd_migrator;
\connect gestion_deudas

REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT USAGE, CREATE ON SCHEMA public TO gd_migrator;
GRANT USAGE ON SCHEMA public TO gd_app;

-- Todo objeto que cree gd_migrator queda con permisos limitados para gd_app
ALTER DEFAULT PRIVILEGES FOR ROLE gd_migrator IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE ON TABLES TO gd_app;
ALTER DEFAULT PRIVILEGES FOR ROLE gd_migrator IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO gd_app;
