package com.example.demo.ModuloVentas;

import java.util.List;

public interface VentaServicio {
    List<Venta> ListarVenta();

    Venta guardarVenta(Venta venta);

    void Buscarbyid(Long id);

    void deleteVenta(Long id);

    Venta buscarVenta(Long id);


}
