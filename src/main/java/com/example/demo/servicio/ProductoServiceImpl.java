package com.example.demo.servicio;

import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoServicio{

    @Autowired
    private ProductoRepositorio repositorio;

    @Override
    public List<Productos> listarProductos() {
        return repositorio.findAll();
    }

    @Override
    public Productos save(Productos producto) {
        return repositorio.save(producto);
    }

    @Override
    public Productos productoById(Long id) {
        return repositorio.findById(id).orElseThrow(()-> new RuntimeException("producto no encontrado"));
    }

    @Override
    public void deleteProductoById(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    public void updateProductro(Productos producto) {
        repositorio.save(producto);
    }


}
