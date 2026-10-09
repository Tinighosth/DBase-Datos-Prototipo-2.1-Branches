-- =====================================================================
-- V2__inmutabilidad_y_bloqueo_delete.sql
-- * Bloquea DELETE en todas las tablas (se usa activo = 0)
-- * Hace inmutable el libro de pagos (sin UPDATE ni TRUNCATE)
-- =====================================================================

CREATE OR REPLACE FUNCTION fn_bloquear_delete() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'DELETE no permitido en %: use activo = 0', TG_TABLE_NAME
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$;

CREATE OR REPLACE FUNCTION fn_bloquear_update() RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Los registros de % son inmutables', TG_TABLE_NAME
        USING ERRCODE = 'integrity_constraint_violation';
END;
$$;

CREATE TRIGGER trg_personas_no_delete     BEFORE DELETE ON personas     FOR EACH ROW EXECUTE FUNCTION fn_bloquear_delete();
CREATE TRIGGER trg_clientes_no_delete     BEFORE DELETE ON clientes     FOR EACH ROW EXECUTE FUNCTION fn_bloquear_delete();
CREATE TRIGGER trg_admin_no_delete        BEFORE DELETE ON admin        FOR EACH ROW EXECUTE FUNCTION fn_bloquear_delete();
CREATE TRIGGER trg_deudas_no_delete       BEFORE DELETE ON deudas       FOR EACH ROW EXECUTE FUNCTION fn_bloquear_delete();
CREATE TRIGGER trg_pagos_no_delete        BEFORE DELETE ON pagos        FOR EACH ROW EXECUTE FUNCTION fn_bloquear_delete();
CREATE TRIGGER trg_total_dinero_no_delete BEFORE DELETE ON total_dinero FOR EACH ROW EXECUTE FUNCTION fn_bloquear_delete();

CREATE TRIGGER trg_pagos_no_update   BEFORE UPDATE   ON pagos FOR EACH ROW       EXECUTE FUNCTION fn_bloquear_update();
CREATE TRIGGER trg_pagos_no_truncate BEFORE TRUNCATE ON pagos FOR EACH STATEMENT EXECUTE FUNCTION fn_bloquear_delete();

-- Defensa extra: el rol de la aplicación (si existe) no puede ni intentar UPDATE en pagos
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'gd_app') THEN
        REVOKE UPDATE ON pagos FROM gd_app;
    END IF;
END;
$$;
