package com.example.demo.ModuloVentas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    @Query("SELECT SUM(v.total) FROM Venta v")
    BigDecimal sumaDeVentas();

    @Query("SELECT SUM(v.total) FROM Venta v WHERE EXTRACT( MONTH from v.fechaVenta) = :mes AND EXTRACT (YEAR from v.fechaVenta) = :anio")
    BigDecimal sumaPorMes(@Param("mes") int mes, @Param("anio") int anio);

    @Query(value = "SELECT MIN(v.fechaVenta) FROM Venta v GROUP BY EXTRACT(MONTH FROM v.fechaVenta), " +
            "EXTRACT(YEAR FROM v.fechaVenta) ORDER BY MIN(v.fechaVenta) ASC")
    List<LocalDateTime> listarFechasUnicasPorMes();

    @Query(value = "SELECT SUM(v.total) FROM Venta v GROUP BY EXTRACT(MONTH FROM v.fechaVenta), " +
            "EXTRACT(YEAR FROM v.fechaVenta) ORDER BY MIN(v.fechaVenta) ASC")
    List<BigDecimal> listarTotalesAgrupadosPorMes();

    @Query(value = "SELECT p.nombre, SUM(d.cantidad) as productos_vendidos \n" +
            "from productos as p, detalle_venta as d \n" +
            "WHERE p.id = d.producto_id\n" +
            "GROUP by p.nombre\n" +
            "ORDER by productos_vendidos DESC", nativeQuery = true)
    List<Object[]> listarProductosVendidos();

    @Query(value = "SELECT\n" +
            "  SUM(total) AS total_ventas\n" +
            "FROM venta\n" +
            "WHERE DATE_TRUNC('month', fecha_venta) = DATE_TRUNC('month', CURRENT_DATE)", nativeQuery = true)
    BigDecimal TotaVentasMes();

    @Query(value = "SELECT SUM(producto_id) as productos_ventasHoy \n" +
            "  from detalle_venta  as d, venta as v\n" +
            "where DATE_TRUNC('day',v.fecha_venta) = DATE_trunc('day',CURRENT_DATE) and d.id = v.id", nativeQuery = true)
    Long SumaVentasPorDia();

    @Query(value = "SELECT v.metodo_pago as  Metodos, SUM(v.total) as totalpor_metodo\n" +
            "FROM venta as v\n" +
            "GROUP BY metodos\n" +
            "ORDER BY totalpor_metodo  ASC", nativeQuery = true)
    List<Object[]>ListaMetodosPago();

}
