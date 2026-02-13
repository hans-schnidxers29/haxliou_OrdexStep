-- =====================================================
-- Script de Índices Compuestos para Multi-Tenancy
-- Mejora el rendimiento de queries filtradas por empresa
-- =====================================================

-- Índices para tabla productos
CREATE INDEX IF NOT EXISTS idx_productos_empresa_estado 
    ON productos(empresa_id, estado);

CREATE INDEX IF NOT EXISTS idx_productos_empresa_categoria 
    ON productos(empresa_id, categoria_id);

CREATE INDEX IF NOT EXISTS idx_productos_empresa_nombre 
    ON productos(empresa_id, nombre);

-- Índices para tabla pedidos
CREATE INDEX IF NOT EXISTS idx_pedidos_empresa_fecha 
    ON pedidos(empresa_id, fecha_pedido DESC);

CREATE INDEX IF NOT EXISTS idx_pedidos_empresa_estado 
    ON pedidos(empresa_id, estado);

CREATE INDEX IF NOT EXISTS idx_pedidos_empresa_cliente 
    ON pedidos(empresa_id, id_cliente);

-- Índices para tabla venta
CREATE INDEX IF NOT EXISTS idx_venta_empresa_fecha 
    ON venta(empresa_id, fecha_venta DESC);

CREATE INDEX IF NOT EXISTS idx_venta_empresa_cliente 
    ON venta(empresa_id, cliente_id);

-- Índices para tabla compras
CREATE INDEX IF NOT EXISTS idx_compras_empresa_fecha 
    ON compras(empresa_id, fecha_compra DESC);

CREATE INDEX IF NOT EXISTS idx_compras_empresa_estado 
    ON compras(empresa_id, estado);

-- Índices para tabla cliente
CREATE INDEX IF NOT EXISTS idx_cliente_empresa_nombre 
    ON cliente(empresa_id, nombre);

CREATE INDEX IF NOT EXISTS idx_cliente_empresa_identificacion 
    ON cliente(empresa_id, numero_identificacion);

-- Índices para tabla proveedores
CREATE INDEX IF NOT EXISTS idx_proveedores_empresa_estado 
    ON proveedores(empresa_id, estado);

-- Índices para tabla categoria
CREATE INDEX IF NOT EXISTS idx_categoria_empresa_estado 
    ON categoria(empresa_id, estado);

-- Índices para tabla cierre_caja
CREATE INDEX IF NOT EXISTS idx_caja_empresa_fecha 
    ON cierre_caja(empresa_id, fecha_cierre DESC);

CREATE INDEX IF NOT EXISTS idx_caja_empresa_estado 
    ON cierre_caja(empresa_id, estado);

-- Índices para tabla egresos
CREATE INDEX IF NOT EXISTS idx_egresos_empresa_fecha 
    ON egresos(empresa_id, fecha_registro DESC);

-- =====================================================
-- Verificar índices creados
-- =====================================================
-- SELECT 
--     tablename, 
--     indexname, 
--     indexdef 
-- FROM pg_indexes 
-- WHERE schemaname = 'public' 
--   AND indexname LIKE 'idx_%empresa%'
-- ORDER BY tablename, indexname;
