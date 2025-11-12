package com.example.demo.repositorio;

import com.example.demo.entidad.EstadoPedido;
import com.example.demo.entidad.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepositorio extends JpaRepository<Pedidos,Long> {

    long count();

    @Query("SELECT COUNT(p) FROM Pedidos p WHERE p.estado = :estado")
    long contarPorEstado(@Param("estado") EstadoPedido estado);
}
