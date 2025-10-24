package com.example.demo.servicio;

import com.example.demo.entidad.DetallePedido;
import com.example.demo.repositorio.DetalleReposirotio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetalleServiceimpl implements DetalleService{


    @Autowired
    private DetalleReposirotio repositorio;

    @Override
    public List<DetallePedido> listarDetalle() {
        return repositorio.findAll();
    }

    @Override
    public DetallePedido guardarDetalle(DetallePedido detallePedido) {
        return null;
    }

    @Override
    public void Buscarbyid(Long id) {

    }

    @Override
    public void deleteDetalleById(Long id) {

    }

    @Override
    public void updateDetalle(DetallePedido detallePedido) {

    }

    @Override
    public DetallePedido detalleById(Long id) {
        return null;
    }

    @Override
    public List<DetallePedido> listarDetalleByPedidoId(Long id) {
        return List.of();
    }
}
