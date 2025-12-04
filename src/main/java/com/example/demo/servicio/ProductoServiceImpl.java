package com.example.demo.servicio;

import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.ProductoRepositorio;
import jakarta.transaction.Transactional;
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

    @Transactional
    @Override
    public void updateProductro(Long id,Productos producto) {
        Productos p1 = repositorio.findById(id).orElseThrow(()->new RuntimeException("producto no encontrado"));
        p1.setId(producto.getId());
        p1.setNombre(producto.getNombre());
        p1.setCantidad(producto.getCantidad());
        p1.setPrecio(producto.getPrecio());
        p1.setDescripcion(producto.getDescripcion());
        if(producto.getCategoria() != null){
            p1.setCategoria(producto.getCategoria());
        }
        repositorio.save(p1);
    }



}
