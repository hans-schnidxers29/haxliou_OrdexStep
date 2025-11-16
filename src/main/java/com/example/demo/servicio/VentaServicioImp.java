package com.example.demo.servicio;


import com.example.demo.entidad.Venta;
import com.example.demo.repositorio.VentaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VentaServicioImp implements VentaServicio{

    @Autowired
    private VentaRepositorio repositorioVenta;


    @Override
    public List<Venta>ListarVenta() {
        return repositorioVenta.findAll();
    }

    @Override
    public Venta guardarVenta(Venta venta) {
        return null;
    }

    @Override
    public void Buscarbyid(Long id) {

    }

    @Override
    public void deleteVenta(Long id) {

    }
}
