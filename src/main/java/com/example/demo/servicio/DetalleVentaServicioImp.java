package com.example.demo.servicio;

import com.example.demo.entidad.DetalleVenta;
import com.example.demo.repositorio.DetalleVentaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalleVentaServicioImp implements DetalleVentaServicio {

    @Autowired
    private DetalleVentaRepositorio detalleVentaRepositorio;


    @Override
    public List<DetalleVenta> ListardetalleVenta() {
        return detalleVentaRepositorio.findAll();
    }

    @Override
    public void Guardardetalle(DetalleVenta detalleVenta) {

    }

    @Override
    public void Update(Long id, DetalleVenta detalleVenta) {

    }

    @Override
    public void delete(Long id) {

    }
}
