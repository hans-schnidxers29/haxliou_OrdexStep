package com.example.demo.ModuloVentas;


import com.example.demo.ModuloVentas.DetalleVenta.DetalleVenta;
import com.example.demo.ModuloVentas.DetalleVenta.DetalleVentaRepositorio;
import com.example.demo.entidad.Productos;
import com.example.demo.servicio.ProductoServicio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class VentaServicioImp implements VentaServicio {


    @Autowired
    private VentaRepositorio repositorioVenta;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private DetalleVentaRepositorio detalleVentaRepositorio;

    @Override
    public List<Venta> ListarVenta() {
        return repositorioVenta.findAll();
    }

    @Override
    public Venta guardarVenta(Venta venta) {
        return repositorioVenta.save(venta);
    }

    @Override
    public void Buscarbyid(Long id) {

    }

    @Override
    public void deleteVenta(Long id) {

    }

    @Override
    public Venta buscarVenta(Long id) {
        return repositorioVenta.findById(id).orElseThrow(() -> new RuntimeException("No existe la venta"));
    }

    @Override
    @Transactional
    public void DescontarStock(Venta venta) {
        for (DetalleVenta detalle : venta.getDetalles()) {

            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                continue;
            }

            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                continue;
            }

            // Buscar el producto completo desde la BD
            Productos productoBD = productoServicio.productoById(detalle.getProducto().getId());

            // Verificar stock suficiente
            if (productoBD.getCantidad() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + productoBD.getNombre() +
                        ". Disponible: " + productoBD.getCantidad() +
                        ", Solicitado: " + detalle.getCantidad());
            }

            // Descontar stock
            int nuevaCantidad = productoBD.getCantidad() - detalle.getCantidad();
            productoBD.setCantidad(nuevaCantidad);

            // Asociar el producto completo al detalle
            detalle.setProducto(productoBD);

            // Guardar producto actualizado
            productoServicio.save(productoBD);

            System.out.println("Stock actualizado - Producto: " + productoBD.getNombre() +
                    " - Cantidad anterior: " + (productoBD.getCantidad() + detalle.getCantidad()) +
                    " - Cantidad nueva: " + productoBD.getCantidad());
        }
    }

    @Override
    public void descontarStock(Venta venta) {

    }

    @Override
    public Long totalVentas() {
        return repositorioVenta.sumaDeVentas();
    }

    @Override
    public BigDecimal sumapormes(int mes, int anio) {
        BigDecimal totalmess= repositorioVenta.sumaPorMes(mes, anio);
        return totalmess != null ? totalmess : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal sumaproductos() {
        return detalleVentaRepositorio.sumaproductos();
    }

    @Override
    public Long sumaproductosPordia(LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay(); // 00:00:00
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay(); // 00:00:00 del día siguiente
        return detalleVentaRepositorio.sumaProductosPorDia(inicio, fin);
    }

    @Override
    public List<String> ListaMeses() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES"));
        return repositorioVenta.listarFechas()
                .stream()
                .toList();
    }

    @Override
    public List<BigDecimal> listarTotalVentas() {
        return repositorioVenta.ListaTotalVentas().stream().toList();
    }
}
