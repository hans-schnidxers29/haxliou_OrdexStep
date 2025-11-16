package com.example.demo.repositorio;

import com.example.demo.entidad.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentaRepositorio extends JpaRepository<Venta,Long> {
}
