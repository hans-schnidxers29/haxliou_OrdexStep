package com.example.demo.servicio;

import com.example.demo.entidad.Pedidos;
import org.springframework.stereotype.Service;

import java.util.List;

public interface PedidoService {

    List<Pedidos> listarpedidos();
    Pedidos guardarpedidos(Pedidos pedidos);
    void deletepedidos(Long id);
    Pedidos pedidosByid(Long id);
    void Updatepedido(Pedidos pedidos);

}
