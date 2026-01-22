package com.example.demo.servicio;

import com.example.demo.entidad.Categoria;

import java.util.List;
import java.util.Map;

public interface CategoriaService {
    List<Categoria> Listarcategoria();
    Categoria savecategoria(Categoria categoria);
    Categoria Buscarbyid(Long id);
    List<Map<String,Object>>Categorias();
}
