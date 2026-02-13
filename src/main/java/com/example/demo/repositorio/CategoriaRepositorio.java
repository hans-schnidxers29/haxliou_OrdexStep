package com.example.demo.repositorio;

import com.example.demo.entidad.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface CategoriaRepositorio extends JpaRepository<Categoria,Long> {

    List<Categoria> findByEstado(boolean estado);
}
