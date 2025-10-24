package com.example.demo.servicio;

import com.example.demo.entidad.Pedidos;
import com.example.demo.repositorio.PedidoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidosServiceImp implements PedidoService{

    @Autowired
    private PedidoRepositorio repositorio;

    @Override
    public List<Pedidos> listarpedidos() {
        return repositorio.findAll();
    }

    @Override
    public Pedidos guardarpedidos(Pedidos pedidos) {
        return repositorio.save(pedidos);
    }

    @Override
    public void deletepedidos(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    public Pedidos pedidosByid(Long id) {
        return repositorio.findById(id).orElse(null);
    }

    @Override
    public void Updatepedido(Pedidos pedidos) {
            repositorio.save(pedidos);
    }
}
