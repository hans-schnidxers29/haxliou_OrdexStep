package com.example.demo.repositorio;

import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.entidad.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@EnableJpaRepositories
public interface ProductoRepositorio extends JpaRepository<Productos,Long> {

    List<Productos>findByEmpresaIdAndEstado(Long empresaId,boolean estado);

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(codigo, 6) AS INTEGER)), 0) " +
            "FROM productos WHERE empresa_id = :empresaId", nativeQuery = true)
    Long obtenerMaximoCodigoNumerico(@Param("empresaId") Long empresaId);

    // REPOSITORIO: Productos MAS vendidos en el mes
    @Query(value = "SELECT nombre, SUM(total_cantidad) as productos_vendidos FROM (" +
            "    SELECT p.nombre, SUM(d.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_venta d ON p.id = d.producto_id " +
            "    INNER JOIN venta v ON v.id = d.venta_id " +
            "    WHERE p.empresa_id = :empresaId AND v.fecha_venta BETWEEN :inicio AND :fin " +
            "    GROUP BY p.nombre " +
            "    UNION ALL " +
            "    SELECT p.nombre, SUM(dp.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_pedido dp ON p.id = dp.producto_id " +
            "    INNER JOIN pedidos pe ON pe.id = dp.pedido_id " +
            "    WHERE p.empresa_id = :empresaId AND pe.fecha_pedido BETWEEN :inicio AND :fin " +
            "    GROUP BY p.nombre " +
            ") as consolidado " +
            "GROUP BY nombre " +
            "ORDER BY productos_vendidos DESC LIMIT 5", nativeQuery = true)
    List<Object[]> ListarProductosMasVendidos(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // REPOSITORIO: Producto MENOS vendido en el mes
    @Query(value = "SELECT nombre, SUM(total_cantidad) as productos_vendidos FROM (" +
            "    SELECT p.nombre, SUM(d.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_venta d ON p.id = d.producto_id " +
            "    INNER JOIN venta v ON v.id = d.venta_id " +
            "    WHERE p.empresa_id = :empresaId AND v.fecha_venta BETWEEN :inicio AND :fin " +
            "    GROUP BY p.nombre " +
            "    UNION ALL " +
            "    SELECT p.nombre, SUM(dp.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_pedido dp ON p.id = dp.producto_id " +
            "    INNER JOIN pedidos pe ON pe.id = dp.pedido_id " +
            "    WHERE p.empresa_id = :empresaId AND pe.fecha_pedido BETWEEN :inicio AND :fin " +
            "    GROUP BY p.nombre " +
            ") as consolidado " +
            "GROUP BY nombre " +
            "ORDER BY productos_vendidos ASC LIMIT 1", nativeQuery = true)
    List<Object[]> ProductoMenosVendido(@Param("empresaId") Long empresaId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT nombre, SUM(total_cantidad) as productos_vendidos " +
            "FROM (" +
            "    -- Suma de Ventas del mes " +
            "    SELECT p.nombre, SUM(d.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_venta d ON p.id = d.producto_id " +
            "    INNER JOIN ventas v ON d.venta_id = v.id " +
            "    WHERE v.fecha_venta BETWEEN :inicio AND :fin AND v.empresa_id = :empresaId " +
            "    GROUP BY p.nombre " +
            "    UNION ALL " +
            "    -- Suma de Pedidos del mes " +
            "    SELECT p.nombre, SUM(dp.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_pedido dp ON p.id = dp.producto_id " +
            "    INNER JOIN pedidos pe ON dp.pedido_id = pe.id " +
            "    WHERE pe.fecha_pedido BETWEEN :inicio AND :fin AND pe.empresa_id = :empresaId " +
            "    GROUP BY p.nombre " +
            ") as consolidado " +
            "GROUP BY nombre " +
            "ORDER BY productos_vendidos DESC LIMIT 5", nativeQuery = true)
    List<Object[]> ListarTopProductosMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("empresaId") Long empresaId);

    @Query("SELECT SUM(dv.cantidad * p.precioCompra) " +
            "FROM DetalleVenta dv " +
            "JOIN dv.producto p " +
            "WHERE dv.venta.fechaVenta BETWEEN :inicio AND :fin AND dv.venta.empresa.id = :empresaId")
    BigDecimal sumaCostoVendidoMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("empresaId") Long empresaId);

    // En PedidoRepository.java
    @Query("SELECT SUM(dp.cantidad * p.precioCompra) " +
            "FROM DetallePedido dp " +
            "JOIN dp.producto p " +
            "WHERE dp.pedido.fechaPedido BETWEEN :inicio AND :fin " +
            "AND dp.pedido.estado = :estado AND dp.pedido.empresa.id = :empresaId")
    BigDecimal sumaCostoPedidosMes(@Param("inicio") LocalDateTime inicio,
                                   @Param("fin") LocalDateTime fin,
                                   @Param("estado") EstadoPedido estado,
                                   @Param("empresaId") Long empresaId);

    // En ProductoRepository.java
    @Query("SELECT SUM(p.cantidad * p.precioCompra) FROM Productos p WHERE p.empresa.id = :empresaId")
    BigDecimal calcularValorInventarioTotal(@Param("empresaId") Long empresaId);

    @Query(value = "select p FROM Productos p where p.cantidad<= p.stockMinimo AND p.empresa.id = :empresaId")
    List<Productos>StockBajoList(@Param("empresaId") Long empresaId);
}
