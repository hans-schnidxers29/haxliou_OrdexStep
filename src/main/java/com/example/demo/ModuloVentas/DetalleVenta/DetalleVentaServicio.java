package com.example.demo.ModuloVentas.DetalleVenta;

import java.util.List;

public interface DetalleVentaServicio {
    List<DetalleVenta> ListardetalleVenta();
    void Guardardetalle(DetalleVenta detalleVenta);
    void Update(Long id, DetalleVenta detalleVenta);
    void delete(Long id);
}
