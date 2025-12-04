package com.example.demo.servicio;

import com.example.demo.entidad.EstadoPedido;
import com.example.demo.entidad.Pedidos;

import java.util.List;

public interface PedidoService {

    List<Pedidos> listarpedidos();
    Pedidos guardarpedidos(Pedidos pedidos);
    void deletepedidos(Long id);
    Pedidos pedidosByid(Long id);
    void Updatepedido(Long id,Pedidos pedidos);
    long ContarPorestados(EstadoPedido estadoPedido);
    void DescantorStock(Pedidos pedidos);
    Long estadoCancelado(EstadoPedido estadoPedido);
    Long estadoCEntregado(EstadoPedido estadoPedido);
}
