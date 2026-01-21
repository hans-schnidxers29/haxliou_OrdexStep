package com.example.demo.servicio;

import com.example.demo.entidad.Productos;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductoServicio {
    List<Productos> listarProductos();
    Productos save(Productos producto);
    Productos productoById(Long id);
    void deleteProductoById(Long id);
    void updateProductro(Long id,Productos producto);
    List<String>NombreProductosVentas();
    List<BigDecimal>CantidadProductosVentas();
    List<Object[]>verificarStock();
    void AgregarStock(Long id,BigDecimal cantidad, BigDecimal NuevoImpuesto,BigDecimal precioCompra);
    List<Map<String,Object>>ProductoSimple();

}
