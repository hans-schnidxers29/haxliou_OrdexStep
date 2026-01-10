package com.example.demo.servicio;

import com.example.demo.entidad.Compras;

import java.util.List;

public interface CompraServicio {

    void saveCompra(Compras compras);
    List<Compras> listarCompra();
    Compras compraById(Long id);
    void deleteCompraById(Long id);
    void updateCompra(Long id,Compras compras);
    boolean verifcarCompra(Long id);
}
