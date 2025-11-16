package com.example.demo.servicio;

import com.example.demo.entidad.Categoria;

import java.util.List;

public interface CategoriaService {
    List<Categoria> Listarcategoria();
    Categoria savecategoria(Categoria categoria);
    Categoria Buscarbyid(Long id);
}
