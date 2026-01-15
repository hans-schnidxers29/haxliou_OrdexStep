package com.example.demo.repositorio;

import com.example.demo.entidad.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductoRepositorio extends JpaRepository<Productos,Long> {

    @Query(value = "SELECT nombre, SUM(total_cantidad) as productos_vendidos \n" +
            "FROM (\n" +
            "   -- Suma de Ventas\n" +
            "    SELECT p.nombre, SUM(d.cantidad) as total_cantidad\n" +
            "    FROM productos p\n" +
            "    INNER JOIN detalle_venta d ON p.id = d.producto_id\n" +
            "    GROUP BY p.nombre\n" +
            "\n" +
            "    UNION ALL\n" +
            "\n" +
            "  \n" +
            "  -- Suma de Pedidos\n" +
            "    SELECT p.nombre, SUM(dp.cantidad) as total_cantidad\n" +
            "    FROM productos p\n" +
            "    INNER JOIN detalle_pedido dp ON p.id = dp.producto_id\n" +
            "    GROUP BY p.nombre\n" +
            ") as consolidado\n" +
            "GROUP BY nombre\n" +
            "ORDER BY nombre DESC LIMIT 5", nativeQuery = true)
    List<Object[]> ListarProductosMasVendidos();

    @Query(value = "SELECT nombre, SUM(total_cantidad) as productos_vendidos " +
            "FROM (" +
            "    -- Suma de Ventas del mes " +
            "    SELECT p.nombre, SUM(d.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_venta d ON p.id = d.producto_id " +
            "    INNER JOIN ventas v ON d.venta_id = v.id " +
            "    WHERE v.fecha_venta BETWEEN :inicio AND :fin " +
            "    GROUP BY p.nombre " +
            "    UNION ALL " +
            "    -- Suma de Pedidos del mes " +
            "    SELECT p.nombre, SUM(dp.cantidad) as total_cantidad " +
            "    FROM productos p " +
            "    INNER JOIN detalle_pedido dp ON p.id = dp.producto_id " +
            "    INNER JOIN pedidos pe ON dp.pedido_id = pe.id " +
            "    WHERE pe.fecha_pedido BETWEEN :inicio AND :fin " +
            "    GROUP BY p.nombre " +
            ") as consolidado " +
            "GROUP BY nombre " +
            "ORDER BY productos_vendidos DESC LIMIT 5", nativeQuery = true)
    List<Object[]> ListarTopProductosMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT nombre, cantidad from productos WHERE cantidad <= 10 ",nativeQuery = true)
    List<Object[]>StockBajo();
}
