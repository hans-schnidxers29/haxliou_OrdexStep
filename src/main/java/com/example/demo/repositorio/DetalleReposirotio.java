package com.example.demo.repositorio;

import com.example.demo.entidad.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetalleReposirotio extends JpaRepository<DetallePedido,Long> {
}
