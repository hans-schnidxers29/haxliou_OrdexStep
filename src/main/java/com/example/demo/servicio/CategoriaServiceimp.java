package com.example.demo.servicio;

import com.example.demo.entidad.Categoria;
import com.example.demo.repositorio.CategoriaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaServiceimp implements CategoriaService{


    @Autowired
    private CategoriaRepositorio repositorio;


    @Override
    public List<Categoria> Listarcategoria() {
        return repositorio.findAll();
    }

    @Override
    public Categoria savecategoria(Categoria categoria) {
        return repositorio.save(categoria);
    }

    @Override
    public Categoria Buscarbyid(Long id) {
        return repositorio.findById(id)
                .orElseThrow(()->new RuntimeException("categoria no encontrada"));
    }
}
