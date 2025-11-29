package com.example.demo.ModuloVentas;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaServicioImp implements VentaServicio {

    @Autowired
    private VentaRepositorio repositorioVenta;


    @Override
    public List<Venta> ListarVenta() {
        return repositorioVenta.findAll();
    }

    @Override
    public Venta guardarVenta(Venta venta) {
        return repositorioVenta.save(venta);
    }

    @Override
    public void Buscarbyid(Long id) {

    }

    @Override
    public void deleteVenta(Long id) {

    }

    @Override
    public Venta buscarVenta(Long id) {
        return repositorioVenta.findById(id).orElseThrow(() -> new RuntimeException("No existe la venta"));
    }
}
