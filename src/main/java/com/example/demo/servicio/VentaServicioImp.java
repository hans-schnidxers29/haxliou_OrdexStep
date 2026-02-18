package com.example.demo.servicio;


import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.DetalleVenta;
import com.example.demo.repositorio.DetalleVentaRepositorio;
import com.example.demo.entidad.Productos;
import com.example.demo.entidad.Venta;
import com.example.demo.repositorio.VentaRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired private SecurityService securityService;

    @Override
    public List<Venta> ListarVenta() {
        return repositorioVenta.findByEmpresaId(securityService.obtenerEmpresaId());
    }

    @Override
    public Venta guardarVenta(Venta venta) {
        venta.setEmpresa(securityService.ObtenerEmpresa());
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

            // Validación de cantidad nula o menor/igual a cero usando compareTo
            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // Buscar el producto completo desde la BD
            Productos productoBD = productoServicio.productoById(detalle.getProducto().getId());

            // Verificar stock suficiente (productoBD.getCantidad() < detalle.getCantidad())
            // compareTo devuelve: -1 si es menor, 0 si es igual, 1 si es mayor
            if (productoBD.getCantidad().compareTo(detalle.getCantidad()) < 0) {
                throw new RuntimeException("Stock insuficiente para el producto: " + productoBD.getNombre() +
                        ". Disponible: " + productoBD.getCantidad() +
                        ", Solicitado: " + detalle.getCantidad());
            }

            // Guardamos la cantidad anterior para el log
            BigDecimal cantidadAnterior = productoBD.getCantidad();

            // Descontar stock usando .subtract()
            BigDecimal nuevaCantidad = productoBD.getCantidad().subtract(detalle.getCantidad());
            productoBD.setCantidad(nuevaCantidad);

            // Asociar el producto completo al detalle para asegurar la persistencia de la relación
            detalle.setProducto(productoBD);

            // Guardar producto actualizado
            productoServicio.save(productoBD);

            System.out.println("Stock actualizado - Producto: " + productoBD.getNombre() +
                    " - Cantidad anterior: " + cantidadAnterior +
                    " - Cantidad nueva: " + productoBD.getCantidad());
        }
    }

    @Override
    public void descontarStock(Venta venta) {

    }

    @Override
    public BigDecimal totalVentas() {
        ZonedDateTime hoyNegocio = ZonedDateTime.now(ZoneId.of("America/Bogota"));
        LocalDateTime inicio = LocalDate.now().atStartOfDay(); // Hoy a las 00:00
        LocalDateTime fin = inicio.plusDays(1);
        return repositorioVenta.sumaVentasRango(inicio,fin,securityService.obtenerEmpresaId());
    }

    @Override
    public BigDecimal sumapormes(LocalDateTime mes, LocalDateTime anio) {
        BigDecimal totalmess= repositorioVenta.sumaPorMes(mes, anio,securityService.obtenerEmpresaId());
        return totalmess != null ? totalmess : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal sumaproductos() {
        return detalleVentaRepositorio.sumaproductos(securityService.obtenerEmpresaId());
    }

    @Override
    public List<Object[]> sumaproductosPordia() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay(); // Hoy a las 00:00
        LocalDateTime fin = inicio.plusDays(1);
        return repositorioVenta.obtenerVentasPorRango(inicio, fin,securityService.obtenerEmpresaId());
    }

    @Override
    public List<String> ListaMeses() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM", new Locale("es", "ES"));
        return repositorioVenta.listarFechasUnicasPorMes(securityService.obtenerEmpresaId())
                .stream()
                .map(fecha -> fecha.format(formatter))
                .collect(Collectors.toList());
    }

    @Override
    public List<BigDecimal>listarTotalVentas() {
        return repositorioVenta.listarTotalesAgrupadosPorMes(securityService.obtenerEmpresaId()).stream().toList();
    }

    @Override
    public List<String> NombreProductos() {
        List<Object[]>resultado =repositorioVenta.listarProductosVendidos(securityService.obtenerEmpresaId());
        return resultado.stream().map(objeto ->(String) objeto[0]).toList();
    }

    @Override
    public List<Number> CantidadProductos() {
        List<Object[]>resultado =repositorioVenta.listarProductosVendidos(securityService.obtenerEmpresaId());
        return resultado.stream().map(objeto -> (Number) objeto[1]).toList();
    }

    @Override
    public BigDecimal TotalVentasMesActual() {
        return repositorioVenta.TotaVentasMes(securityService.obtenerEmpresaId());
    }

    /**
     * @return Lista de metodos de pago
     */
    @Override
    public List<String> ListaMetodosPago() {
        List<Object[]> resultado = repositorioVenta.ListaMetodosPago(securityService.obtenerEmpresaId());
        return resultado.stream()
                .map(objeto -> objeto[0] == null ? "Desconocido" : objeto[0].toString())
                .toList();
    }

    @Override
    public List<Number> ListaMetodosPagoValores() {
        List<Object[]> resultado = repositorioVenta.ListaMetodosPago(securityService.obtenerEmpresaId());
        return resultado.stream()
                .map(objeto -> {
                    // Convertimos de forma segura a Double para que JS no tenga problemas
                    return (objeto[1] instanceof Number) ? (Number) objeto[1] : 0;
                })
                .toList();
    }

    @Override
    @Transactional
    public void UpdateVenta(Venta venta) {
        repositorioVenta.save(venta);
    }


}
