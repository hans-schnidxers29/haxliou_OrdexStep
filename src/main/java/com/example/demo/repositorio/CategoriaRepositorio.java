package com.example.demo.repositorio;

import com.example.demo.entidad.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepositorio extends JpaRepository<Categoria,Long> {
}
