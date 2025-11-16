package com.example.demo.servicio;

import com.example.demo.entidad.Venta;

import java.util.List;

public interface VentaServicio {
    List<Venta>ListarVenta();
    Venta guardarVenta(Venta venta);
    void Buscarbyid(Long id);
    void deleteVenta(Long id);


}
