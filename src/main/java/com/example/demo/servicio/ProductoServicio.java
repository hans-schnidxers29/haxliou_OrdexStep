package com.example.demo.servicio;

import com.example.demo.entidad.Productos;

import java.util.List;

public interface ProductoServicio {
    List<Productos> listarProductos();
    Productos save(Productos producto);
    Productos productoById(Long id);
    void deleteProductoById(Long id);
    void updateProductro(Productos producto);


}
