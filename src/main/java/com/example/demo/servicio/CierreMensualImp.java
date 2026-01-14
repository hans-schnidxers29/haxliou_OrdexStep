package com.example.demo.servicio;

import com.example.demo.ModuloVentas.VentaRepositorio;
import com.example.demo.entidad.CierreMensual;
import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class CierreMensualImp implements CierreMensualServicio{

    @Autowired
    private CierreMensualRepositorio cierreRepo;
    @Autowired private VentaRepositorio ventaRepo;
    @Autowired private EgresoRepositorio egresoRepo;
    @Autowired private ProductoRepositorio productoRepo;
    @Autowired private ClienteRepositorio clienteRepo;
    @Autowired private ComprasRepositorio comprasRepo;
    @Autowired private PedidoRepositorio pedidoRepo;

    @Override
    public CierreMensual procesarCierreMes(int mes, int anio) {
        return null;
    }


//    @Override
//    @Transactional
//    public CierreMensual procesarCierreMes(int mes, int anio) {
//        // 1. Rango de fechas
//        LocalDate primerDia = LocalDate.of(anio, mes, 1);
//        LocalDateTime inicio = primerDia.atStartOfDay();
//        LocalDateTime fin = primerDia.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
//
//        // 2. Ventas por método de pago (CON VALIDACIÓN NULL)
//        BigDecimal efectivo = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "EFECTIVO"));
//        BigDecimal tarjeta = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "TARJETA"));
//        BigDecimal transferencia = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "TRANSFERENCIA"));
//        BigDecimal mixto = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "MIXTO"));
//        BigDecimal totalVentas = efectivo.add(tarjeta).add(transferencia).add(mixto);
//
//        // 3. Impuestos
//        BigDecimal impVentas = nvl(ventaRepo.sumaImpuestosMes(inicio, fin));
//        BigDecimal impPedidos = nvl(pedidoRepo.sumaImpuestosPedidos(inicio, fin));
//        BigDecimal impuestosTotales = impVentas.add(impPedidos);
//
//        // 4. Costos y Gastos
//        BigDecimal totalEgresos = nvl(egresoRepo.sumarEgresosPorDia(inicio, fin));
//        BigDecimal totalCompras = nvl(comprasRepo.sumTotalCompras(inicio, fin));
//        BigDecimal costoVendido = nvl(ventaRepo.sumaPorMes(inicio, fin)); // NUEVO
//
//        // 5. Inventario (con precio de COSTO, no de venta)
//        List<Productos> productos = productoRepo.findAll();
//        BigDecimal valorInventario = productos.stream()
//                .filter(p -> p.getCantidad().compareTo(BigDecimal.ZERO) > 0) // Ignorar productos agotados o con errores de stock negativo
//                .filter(p -> p.getPrecio() != null && p.getPrecio().compareTo(BigDecimal.ZERO) > 0) // Ignorar sin precio
//                .map(p -> p.getPrecio().multiply(p.getCantidad()))
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//
//        Integer nuevosClientes = clienteRepo.contarNuevosClientesPorRango(inicio, fin);
//
//        // 6. UTILIDAD CORRECTA
//        BigDecimal ventasNetas = totalVentas.subtract(impuestosTotales); // Ventas sin IVA
//        BigDecimal utilidadBruta = ventasNetas.subtract(costoVendido);   // Margen de ganancia
//        BigDecimal utilidadNeta = utilidadBruta.subtract(totalEgresos);  // Después de gastos
//
//        // 7. Evitar duplicados
//        cierreRepo.eliminarCierreExistente(mes, anio);
//
//        // 8. Construir y guardar
//        CierreMensual cierre = new CierreMensual();
//        cierre.setMes(mes);
//        cierre.setAnio(anio);
//        cierre.setFechaCierre(LocalDate.now());
//        cierre.setTotalVentasEfectivo(efectivo);
//        cierre.setTotalVentasTarjeta(tarjeta);
//        cierre.setTotalVentasTransferencia(transferencia);
//        cierre.setRecaudacionTotal(totalVentas);
//        cierre.setRecaudacionBruta(ventasNetas);
//        cierre.setTotalImpuestos(impuestosTotales);
//        cierre.setTotalEgresos(totalEgresos);
//        cierre.setTotalCompras(totalCompras);
//        cierre.setUtilidadBruta(utilidadBruta); // AGREGAR ESTE CAMPO
//        cierre.setUtilidadNeta(utilidadNeta);
//        cierre.setCantidadPedidos(pedidoRepo.cantidadPedidosPorRango(inicio, fin).intValue());
//        cierre.setNuevosClientes(nuevosClientes);
//        cierre.setTotalProductosEnStock(productos.size());
//        cierre.setValorInventarioTotal(valorInventario);
//
//        return cierreRepo.save(cierre);
//    }
//
//    // Método auxiliar para manejar NULL
//    private BigDecimal nvl(BigDecimal value) {
//        return value != null ? value : BigDecimal.ZERO;
//    }
}
