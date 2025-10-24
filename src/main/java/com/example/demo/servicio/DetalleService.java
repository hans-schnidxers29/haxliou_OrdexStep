package com.example.demo.servicio;


import com.example.demo.entidad.DetallePedido;
import org.springframework.stereotype.Service;

import java.util.List;

public interface DetalleService {
    List<DetallePedido>listarDetalle();
    DetallePedido guardarDetalle(DetallePedido detallePedido);
    void Buscarbyid(Long id);
    void deleteDetalleById(Long id);
    void updateDetalle(DetallePedido detallePedido);
    DetallePedido detalleById(Long id);
    List<DetallePedido> listarDetalleByPedidoId(Long id);
}
