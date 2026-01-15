package com.example.demo.repositorio;

import com.example.demo.entidad.DetalleCompra;
import com.example.demo.entidad.Productos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DetalleCompraRepositorio extends JpaRepository<DetalleCompra,Long> {

    Optional<DetalleCompra> findTopByProductosOrderByCompra_FechaCompraDesc(Productos producto);
}
