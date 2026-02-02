package com.example.demo.servicio;

import com.example.demo.entidad.DetalleVenta;

import java.util.List;

public interface DetalleVentaServicio {
    List<DetalleVenta> ListardetalleVenta();
    void Guardardetalle(DetalleVenta detalleVenta);
    void Update(Long id, DetalleVenta detalleVenta);
    void delete(Long id);
}
