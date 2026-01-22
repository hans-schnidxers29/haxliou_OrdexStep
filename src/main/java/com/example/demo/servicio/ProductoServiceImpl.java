package com.example.demo.servicio;

import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.ProductoRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public void updateProductro(Long id, Productos producto) {
        Productos p1 = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        p1.setNombre(producto.getNombre());
        p1.setDescripcion(producto.getDescripcion());
        p1.setPrecio(producto.getPrecio());
        p1.setCantidad(producto.getCantidad());
        p1.setImpuesto(producto.getImpuesto());
        p1.setProveedor(producto.getProveedor());
        p1.setPrecioCompra(producto.getPrecioCompra());
        p1.setTipoVenta(producto.getTipoVenta());
        p1.setStockMinimo(producto.getStockMinimo());
        p1.setIncremento(producto.getIncremento());
        if (producto.getCategoria() != null) {
            p1.setCategoria(producto.getCategoria());
        }
    }

    @Override
    public List<String> NombreProductosVentas() {
        List<Object[]>resultado = repositorio.ListarProductosMasVendidos();
        return resultado.stream().map(objeto ->(String) objeto[0]).toList();
    }

    @Override
    public List<BigDecimal> CantidadProductosVentas() {
        List<Object[]>resultado = repositorio.ListarProductosMasVendidos();
        return resultado.stream().map(objeto ->(BigDecimal) objeto[1]).toList();
    }

    /**
     * @return
     */
    @Override
    public List<Object[]> verificarStock() {
        return repositorio.StockBajo();
    }

    @Transactional
    @Override
    public void AgregarStock(Long id, BigDecimal cantidad,BigDecimal nuevoImpuesto,BigDecimal precioCompraN) {
        Productos producto = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto No Encontrado con ID: " + id));


        BigDecimal stockActual = (producto.getCantidad() == null) ? BigDecimal.ZERO : producto.getCantidad();
        producto.setCantidad(stockActual.add(cantidad));

        if (nuevoImpuesto != null && nuevoImpuesto.compareTo(BigDecimal.ZERO) >= 0) {
            producto.setImpuesto(nuevoImpuesto);
        }
        
        // ACTUALIZACIÓN: Guardar el precio de compra enviado desde la factura
        if (precioCompraN != null && precioCompraN.compareTo(BigDecimal.ZERO) > 0) {
            producto.setPrecioCompra(precioCompraN);
        }

        repositorio.save(producto);
    }

    @Override
    public List<Map<String, Object>> ProductoSimple() {
        List<Map<String, Object>> productosJson = repositorio.findAll().stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("nombre", p.getNombre());
            map.put("precio", p.getPrecio());
            map.put("impuesto", p.getImpuesto());
            map.put("tipoVenta", p.getTipoVenta().name());
            map.put("categoriaId", p.getCategoria().getId() != null ? p.getCategoria().getId() : null);
            return map;
        }).collect(Collectors.toList());
        return productosJson;
    }


}
