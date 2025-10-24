package com.example.demo.repositorio;

import com.example.demo.entidad.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PedidoRepositorio extends JpaRepository<Pedidos,Long> {
}
