-- Solo para una base preexistente creada por la versión con Double.
-- Respaldar primero y detener la aplicación durante la migración.
-- No borra tablas ni filas. Los importes se redondean a dos decimales.
USE bd_littledokyhub;
ALTER TABLE productos MODIFY precio DECIMAL(10,2) NOT NULL;
ALTER TABLE pedidos MODIFY total DECIMAL(10,2) NOT NULL;
ALTER TABLE detalles_pedido MODIFY precio_unitario DECIMAL(10,2) NOT NULL,
    MODIFY subtotal DECIMAL(10,2) NOT NULL;
-- La unión del carrito necesita este índice único. Si existen duplicados,
-- revisar sus cantidades antes de intentar crearlo; este script no los elimina.
SET @indice_existe = (SELECT COUNT(*) FROM (
    SELECT index_name FROM information_schema.statistics
    WHERE table_schema = DATABASE() AND table_name = 'carrito_items' AND non_unique = 0
    GROUP BY index_name
    HAVING GROUP_CONCAT(column_name ORDER BY seq_in_index) = 'carrito_id,producto_id,configuracion'
) AS indices);
SET @sql_indice = IF(@indice_existe > 0, 'SELECT 1',
    'ALTER TABLE carrito_items ADD UNIQUE KEY uq_carrito_config (carrito_id,producto_id,configuracion)');
PREPARE stmt FROM @sql_indice;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
