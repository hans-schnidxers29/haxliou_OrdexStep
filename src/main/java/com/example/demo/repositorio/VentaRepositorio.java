package com.example.demo.repositorio;

import com.example.demo.entidad.Enum.MetodoPago;
import com.example.demo.entidad.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    List<Venta>findByEmpresaId(Long empresaId);

    @Query(value = "SELECT COALESCE(SUM(v.total), 0) " +
            "FROM venta v " +
            "WHERE v.fecha_venta >= :inicio " +
            "  AND v.fecha_venta < :fin AND v.empresa_id = :empresaId",
            nativeQuery = true)
    BigDecimal sumaVentasRango(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.empresa.id = :empresaId")
    BigDecimal sumaPorMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,@Param("empresaId") Long empresaId);

    // Query adicional para los impuestos del mes
    @Query("SELECT COALESCE(SUM(v.total - (v.total / (1 + v.impuesto/100))), 0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.empresa.id = :empresaId" )
    BigDecimal sumaImpuestosMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,@Param("empresaId") Long empresaId);

    @Query("SELECT DISTINCT function('date_trunc', 'month', v.fechaVenta) " +
            "FROM Venta v " +
            "WHERE v.empresa.id = :empresaId " +
            "ORDER BY function('date_trunc', 'month', v.fechaVenta) ASC")
    List<LocalDateTime> listarFechasUnicasPorMes(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT SUM(consolidad.total_mes) " +
            "FROM (" +
            "  SELECT SUM(total) as total_mes, date_trunc('month', fecha_venta) as mes " +
            "  FROM venta WHERE empresa_id = :empresaId GROUP BY mes " +
            "  UNION ALL " +
            "  SELECT SUM(p.total) as total_mes, date_trunc('month', p.fecha_pedido) as mes " +
            "  FROM pedidos p WHERE p.estado = 'ENTREGADO' AND p.empresa_id = :empresaId GROUP BY mes " +
            ") AS consolidad " +
            "GROUP BY consolidad.mes " +
            "ORDER BY consolidad.mes ASC", nativeQuery = true)
    List<BigDecimal> listarTotalesAgrupadosPorMes(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT p.nombre, SUM(d.cantidad) as productos_vendidos " +
            "FROM productos p " +
            "JOIN detalle_venta d ON p.id = d.producto_id " +
            "JOIN venta v ON d.venta_id = v.id " + // Unimos con la tabla de ventas
            "WHERE v.empresa_id = :empresaId " +   // Filtramos por la empresa del plan
            "AND p.empresa_id = :empresaId " +
            "GROUP BY p.nombre " +
            "ORDER BY productos_vendidos DESC", nativeQuery = true)
    List<Object[]> listarProductosVendidos(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT\n" +
            "  SUM(total) AS total_ventas\n" +
            "FROM venta\n" +
            "WHERE DATE_TRUNC('month', fecha_venta) = DATE_TRUNC('month', CURRENT_DATE) AND empresa_id = :empresaId", nativeQuery = true)
    BigDecimal TotaVentasMes(@Param("empresaId") Long empresaId);

    @Query(value = "SELECT " +
            "    COALESCE(SUM(CASE WHEN um.code = '94' THEN d.cantidad ELSE 0 END), 0) as total_unidades, " +
            "    COALESCE(SUM(CASE WHEN um.code = 'KGM' THEN d.cantidad ELSE 0 END), 0) as total_gramos " +
            "FROM detalle_venta d " +
            "JOIN venta v ON d.venta_id = v.id " +
            "JOIN productos p ON d.producto_id = p.id " +
            "JOIN unidad_medida um ON p.unidad_medida_id = um.id " + // Se une con la tabla de unidades
            "WHERE v.fecha_venta >= :inicio " +
            "  AND v.fecha_venta < :fin  AND v.empresa_id = :empresaId AND p.empresa_id = v.empresa_id", nativeQuery = true)
    List<Object[]> obtenerVentasPorRango(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("empresaId") Long empresaId);

    @Query(value = "SELECT v.metodo_pago as  Metodos, SUM(v.total) as totalpor_metodo\n" +
            "FROM venta as v\n" +
            "WHERE v.empresa_id = :empresaId\n" +
            "GROUP BY metodos\n" +
            "ORDER BY totalpor_metodo  ASC", nativeQuery = true)
    List<Object[]>ListaMetodosPago(@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v " +
            "WHERE v.fechaVenta BETWEEN :inicio AND :fin " +
            "AND v.metodoPago = :metodoPago AND v.empresa.id = :empresaId")
    BigDecimal sumaPorMetodoPago(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("metodoPago") String metodoPago,
            @Param("empresaId") Long empresaId
    );

    @Query("SELECT COALESCE((COUNT(v)),0) FROM Venta v WHERE v.fechaVenta BETWEEN :inicio AND :fin AND v.empresa.id = :empresaId")
    BigDecimal CantidadDeVentas(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("empresaId") Long empresaId);

    @Query(value = "select sum(total_ventas_mayor) as total_mayor\n" +
            "from(\n" +
            "    select sum(v.total) as total_ventas_mayor,  date_trunc('month', fecha_venta) as mes\n" +
            "    FROM venta v \n" +
            "    WHERE v.venta_al_por_mayor  = 'TRUE' AND v.empresa_id = :empresaId GROUP BY mes\n" +
            "\n" +
            "    UNION \n" +
            "    SELECT SUM(p.total) as total_ventas_mayor, date_trunc('month', p.fecha_pedido) as mes\n" +
            "    FROM pedidos p \n" +
            "    WHERE p.estado = 'ENTREGADO' AND p.venta_por_mayor = 'TRUE' AND p.empresa_id = :empresaId GROUP BY mes\n" +
            "  \n" +
            ") AS consolidado \n" +
            "GROUP BY mes ORDER BY mes ASC", nativeQuery = true)
    BigDecimal VentasTotalesAlMayor(@Param("empresaId") Long empresaId);

    @Query("select sum (p.monto) from Pagos as p join Venta as v on v.id = p.venta.id where v.metodoPago = 'MIXTO' " +
            "and p.metodoPago = :metodo and p.fechaPago between :inicio and :fin and v.empresa.id = :empresaId")
    BigDecimal  ValoresPorVentasMixtas(@Param("inicio")LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                                       @Param("metodo") MetodoPago metodo, @Param("empresaId") Long empresaId);
}
