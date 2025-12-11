package com.example.demo.ModuloVentas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta, Long> {

    @Query("SELECT SUM(v.total) FROM Venta v")
    Long sumaDeVentas();

    @Query("SELECT SUM(v.total) FROM Venta v WHERE EXTRACT( MONTH from v.fechaVenta) = :mes AND EXTRACT (YEAR from v.fechaVenta) = :anio")
    BigDecimal sumaPorMes(@Param("mes") int mes, @Param("anio") int anio);

}
