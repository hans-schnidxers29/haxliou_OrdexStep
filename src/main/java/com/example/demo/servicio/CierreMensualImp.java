package com.example.demo.servicio;

import com.example.demo.ModuloVentas.VentaRepositorio;
import com.example.demo.entidad.CierreMensual;
import com.example.demo.entidad.DetalleCompra;
import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.Map;

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
    @Autowired private DetalleCompraRepositorio detalleRepo;


    @Override
    @Transactional
    public CierreMensual procesarCierreMes(int mes, int anio) {
        // 1. Rango de fechas
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDateTime inicio = primerDia.atStartOfDay();
        LocalDateTime fin = primerDia.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        // 2. Ventas por método de pago (CON VALIDACIÓN NULL)
        BigDecimal efectivo = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "EFECTIVO"));
        BigDecimal tarjeta = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "TARJETA"));
        BigDecimal transferencia = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "TRANSFERENCIA"));
        BigDecimal mixto = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "MIXTO"));
        BigDecimal totalVentas = efectivo.add(tarjeta).add(transferencia).add(mixto);

        // 3. Impuestos
        BigDecimal impVentas = nvl(ventaRepo.sumaImpuestosMes(inicio, fin));
        BigDecimal impPedidos = nvl(pedidoRepo.sumaImpuestosPedidos(inicio, fin));
        BigDecimal impuestosTotales = impVentas.add(impPedidos);

        // 4. Costos y Gastos
        BigDecimal totalEgresos = nvl(egresoRepo.sumarEgresosPorDia(inicio, fin));
        BigDecimal totalCompras = nvl(comprasRepo.sumTotalCompras(inicio, fin));
        BigDecimal costoVendido = nvl(ventaRepo.sumaPorMes(inicio, fin)); // NUEVO

           BigDecimal valorInventario =productoRepo.findAll().stream().map(p -> {
            // Buscas en los detalles de compra el registro más reciente para este producto
            BigDecimal ultimoCosto = detalleRepo.findTopByProductosOrderByCompra_FechaCompraDesc(p)
                                      .map(DetalleCompra::getPrecioUnitario)
                                      .orElse(BigDecimal.ZERO);
            return ultimoCosto.multiply(p.getCantidad());
        })
        .reduce(BigDecimal.ZERO, BigDecimal::add);

        Integer nuevosClientes = clienteRepo.contarNuevosClientesPorRango(inicio, fin);

        // 6. UTILIDAD CORRECTA
        BigDecimal ventasNetas = totalVentas.subtract(impuestosTotales); // Ventas sin IVA
        BigDecimal utilidadBruta = ventasNetas.subtract(costoVendido);   // Margen de ganancia
        BigDecimal utilidadNeta = utilidadBruta.subtract(totalEgresos);  // Después de gastos

        // 7. Evitar duplicados
        cierreRepo.eliminarCierreExistente(mes, anio);

        // 8. Construir y guardar
        CierreMensual cierre = new CierreMensual();
        cierre.setMes(mes);
        cierre.setAnio(anio);
        cierre.setFechaCierre(LocalDate.now());
        cierre.setTotalVentasEfectivo(efectivo);
        cierre.setTotalVentasTarjeta(tarjeta);
        cierre.setTotalVentasTransferencia(transferencia);
        cierre.setRecaudacionTotal(totalVentas);
        cierre.setRecaudacionBruta(ventasNetas);
        cierre.setTotalImpuestos(impuestosTotales);
        cierre.setTotalEgresos(totalEgresos);
        cierre.setTotalCompras(totalCompras);
        cierre.setUtilidadBruta(utilidadBruta); // AGREGAR ESTE CAMPO
        cierre.setUtilidadNeta(utilidadNeta);
        cierre.setCantidadPedidos(pedidoRepo.cantidadPedidosPorRango(inicio, fin).intValue());
        cierre.setNuevosClientes(nuevosClientes);
        cierre.setTotalProductosEnStock(productoRepo.findAll().size());
        cierre.setValorInventarioTotal(valorInventario);

        return cierreRepo.save(cierre);
    }

    // Método auxiliar para manejar NULL
    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    public Map<String, Object> obtenerResumenProyectado(int mes, int anio) {
        Map<String, Object> resumen = new HashMap<>();
        LocalDateTime fechaInicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fechaFin = fechaInicio.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        BigDecimal utilidadActual = calcularUtilidad(mes, anio);
        int mesAnt= (mes  == 1) ? 12 : mes-1;
        int anioAnt = (mes == 1 ) ? anio - 1: anio;
        BigDecimal utilidadAnterior = calcularUtilidad(mesAnt, anioAnt);

        boolean utilidadSubio = utilidadActual.compareTo(utilidadAnterior) > 0;
        double porcentaje = 0.0;

        if (utilidadAnterior.compareTo(BigDecimal.ZERO) != 0) {
            // (Actual - Anterior) / Anterior * 100
            porcentaje = utilidadActual.subtract(utilidadAnterior)
                    .divide(utilidadAnterior.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }
        // 1. Productos en Stock (Total unidades)
        long stockTotalUnd = productoRepo.findAll().stream().filter(p->p.getUnidadMedida()
                        .equalsIgnoreCase("unidad"))
                .mapToLong(p -> p.getCantidad().longValue()).sum();

        BigDecimal stockTotalkg = productoRepo.findAll().stream().filter(p->p.getUnidadMedida()
                .equalsIgnoreCase("kg")).map(Productos::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorInventario = productoRepo.findAll().stream().map(p -> {
                    BigDecimal ultimoCosto = detalleRepo.findTopByProductosOrderByCompra_FechaCompraDesc(p)
                            .map(DetalleCompra::getPrecioUnitario)
                            .orElse(p.getPrecio());
                    return ultimoCosto.multiply(p.getCantidad());
                }).reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Utilidad Proyectada (Ventas - Costos - Egresos del mes actual)
        BigDecimal ventas = ventaRepo.sumaPorMes(fechaInicio, fechaFin); // Implementar en repo
        BigDecimal egresos = egresoRepo.sumarEgresosPorDia(fechaInicio, fechaFin); // Implementar en repo
        BigDecimal utilidad = ventas.subtract(valorInventario).subtract(egresos);

        resumen.put("stockUnidades", stockTotalUnd);
        resumen.put("stockPesable", stockTotalkg);
        resumen.put("inversion", valorInventario);
        resumen.put("gastos", egresos);
        resumen.put("utilidad", utilidadActual);
        resumen.put("utilidadSubio", utilidadSubio);
        resumen.put("porcentajeUtilidad", Math.abs(porcentaje));

        return resumen;
    }

    @Override
    public BigDecimal calcularUtilidad(int mes, int anio) {
        // 1. Definir rango de fechas para el mes solicitado
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59);

        // 2. Obtener Ventas Totales
        BigDecimal ventas = ventaRepo.sumaVentasRango(inicio, fin);
        if (ventas == null) ventas = BigDecimal.ZERO;

        // 3. Obtener Costo de lo Vendido (CPV)
        // Esto es lo que evita que la utilidad salga negativa por stock estancado
        BigDecimal costoVentas = productoRepo.findAll().stream().map(p -> {
            BigDecimal ultimoCosto = detalleRepo.findTopByProductosOrderByCompra_FechaCompraDesc(p)
                    .map(DetalleCompra::getPrecioUnitario)
                    .orElse(p.getPrecio());
            return ultimoCosto.multiply(p.getCantidad());
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (costoVentas == null) costoVentas = BigDecimal.ZERO;

        // 4. Obtener Gastos (Egresos)
        BigDecimal gastos = egresoRepo.sumarEgresosPorDia(inicio, fin);
        if (gastos == null) gastos = BigDecimal.ZERO;

        // Formula: Ventas - Costo de productos vendidos - Gastos operativos
        return ventas.subtract(costoVentas).subtract(gastos);
    }
}

