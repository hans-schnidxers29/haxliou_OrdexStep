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
    Long sumaDeVentas();

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


}
