package com.example.demo.servicio;

import com.example.demo.entidad.Compras;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface CompraServicio {

    void saveCompra(Compras compras);
    List<Compras> listarCompra();
    Compras compraById(Long id);
    void deleteCompraById(Long id);
    void updateCompra(Long id,Compras compras);
    boolean verifcarCompra(Long id);
    String GenerarReferenciasDeCompras();
    void ConfirmarCompra(Long id);
    void AnularCompra(Long id);
    Map<String,Object>StokMensual(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}
