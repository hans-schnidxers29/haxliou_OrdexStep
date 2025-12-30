package com.example.demo.ModuloVentas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface VentaServicio {
    List<Venta> ListarVenta();
    Venta guardarVenta(Venta venta);
    void Buscarbyid(Long id);
    void deleteVenta(Long id);
    Venta buscarVenta(Long id);
    void DescontarStock(Venta venta);
    void descontarStock(Venta venta);
    Long totalVentas();
    BigDecimal sumapormes(int mes, int anio);
    BigDecimal sumaproductos();
    Long  sumaproductosPordia(LocalDate fecha);
    List<String> ListaMeses();
    List<BigDecimal> listarTotalVentas();
}
