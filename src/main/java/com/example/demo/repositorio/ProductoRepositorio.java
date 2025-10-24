package com.example.demo.repositorio;

import com.example.demo.entidad.Productos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepositorio extends JpaRepository<Productos,Long> {
}
