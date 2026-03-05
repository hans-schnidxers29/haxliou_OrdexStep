package com.example.demo.servicio;

import com.example.demo.entidad.Productos;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ProductoServicio {
    List<Productos> listarProductos();
    Productos save(Productos producto);
    Productos productoById(Long id);
    void deleteProductoById(Long id) throws Exception;
    void updateProductro(Long id,Productos producto);
    List<String>NombreProductosVentas(LocalDateTime inicio, LocalDateTime fin);
    List<BigDecimal>CantidadProductosVentas(LocalDateTime inicio, LocalDateTime fin);
    List<Productos> verificarStock();
    void AgregarStock(Long id,BigDecimal cantidad, BigDecimal NuevoImpuesto,BigDecimal precioCompra);
    List<Map<String,Object>>ProductoSimple();
    void DescontarStock(BigDecimal cantidad, Long id);

}
