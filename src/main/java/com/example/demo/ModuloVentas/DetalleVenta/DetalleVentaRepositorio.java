package com.example.demo.ModuloVentas.DetalleVenta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface DetalleVentaRepositorio extends JpaRepository<DetalleVenta, Long> {

    @Query("SELECT  SUM(cantidad) FROM DetalleVenta")
    BigDecimal sumaproductos();

}
