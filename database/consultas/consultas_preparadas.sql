-- =====================================================================
-- consultas_preparadas.sql  |  Referencia para trabajar a mano en psql
-- Siempre con parámetros ($1, $2...). Nunca concatenar texto en el SQL.
-- No hay DELETE: "eliminar" = UPDATE ... SET activo = 0
-- =====================================================================

-- Clientes activos (paginado)
PREPARE clientes_activos (int, int) AS
    SELECT p.id_persona, p.nombre, p.apellido, c.direccion
    FROM personas p
    JOIN clientes c ON c.id_persona = p.id_persona
    WHERE p.activo = 1
    ORDER BY p.id_persona
    LIMIT $1 OFFSET $2;
-- EXECUTE clientes_activos(20, 0);

-- Deudas activas de un cliente
PREPARE deudas_por_cliente (bigint) AS
    SELECT id_deuda, concepto, monto_inicial, saldo_pendiente, fecha_vencimiento,
           CASE estado WHEN 1 THEN 'Pendiente' ELSE 'Pagada' END AS estado
    FROM deudas
    WHERE id_cliente = $1 AND activo = 1
    ORDER BY fecha_vencimiento NULLS LAST;
-- EXECUTE deudas_por_cliente(1);

-- Total de dinero de un cliente
PREPARE total_por_cliente (bigint) AS
    SELECT total_deuda, total_pagado, fecha_actualizacion
    FROM total_dinero
    WHERE id_cliente = $1 AND activo = 1;
-- EXECUTE total_por_cliente(1);

-- Deudas vencidas a una fecha
PREPARE deudas_vencidas (date) AS
    SELECT d.id_deuda, p.nombre, p.apellido, d.saldo_pendiente,
           ($1 - d.fecha_vencimiento) AS dias_de_mora
    FROM deudas d
    JOIN personas p ON p.id_persona = d.id_cliente
    WHERE d.activo = 1 AND d.estado = 1 AND d.fecha_vencimiento < $1
    ORDER BY dias_de_mora DESC;
-- EXECUTE deudas_vencidas(CURRENT_DATE);

-- Historial de abonos de una deuda (libro inmutable)
PREPARE pagos_de_deuda (bigint) AS
    SELECT id_pago, monto, saldo_resultante, fecha_pago
    FROM pagos
    WHERE id_deuda = $1
    ORDER BY fecha_pago;
-- EXECUTE pagos_de_deuda(1);

-- Desactivar / reactivar una persona (borrado lógico)
PREPARE cambiar_estado_persona (smallint, bigint) AS
    UPDATE personas SET activo = $1, fecha_actualizacion = now()
    WHERE id_persona = $2
    RETURNING id_persona, activo;
-- EXECUTE cambiar_estado_persona(0, 3);   -- desactivar
-- EXECUTE cambiar_estado_persona(1, 3);   -- reactivar

-- Auditoría: registros inactivos
PREPARE personas_inactivas AS
    SELECT id_persona, nombre, apellido, tipo FROM personas WHERE activo = 0;

-- Limpieza de la sesión
-- DEALLOCATE ALL;
