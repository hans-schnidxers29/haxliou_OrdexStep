package com.example.demo.ModuloVentas;

import java.math.BigDecimal;
import java.util.List;

public interface VentaServicio {
    List<Venta> ListarVenta();
    Venta guardarVenta(Venta venta);
    void Buscarbyid(Long id);
    void deleteVenta(Long id);
    Venta buscarVenta(Long id);
    void DescontarStock(Venta venta);
    void descontarStock(Venta venta);
    BigDecimal totalVentas();
    BigDecimal sumapormes(int mes, int anio);
    BigDecimal sumaproductos();
    List<Object[]>  sumaproductosPordia();
    List<String> ListaMeses();
    List<BigDecimal> listarTotalVentas();
    List<String>NombreProductos();
    List<Number>CantidadProductos();
    BigDecimal TotalVentasMesActual();
    List<String>ListaMetodosPago();
    List<Number> ListaMetodosPagoValores();
}
