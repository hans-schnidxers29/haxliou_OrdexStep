package com.example.demo.servicio;

import com.example.demo.entidad.CierreMensual;
import com.example.demo.entidad.Enum.EstadoPedido;
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

    @Autowired private CierreMensualRepositorio cierreRepo;
    @Autowired private VentaRepositorio ventaRepo;
    @Autowired private EgresoRepositorio egresoRepo;
    @Autowired private ProductoRepositorio productoRepo;
    @Autowired private ClienteRepositorio clienteRepo;
    @Autowired private ComprasRepositorio comprasRepo;
    @Autowired private PedidoRepositorio pedidoRepo;



    @Override
    @Transactional
    public CierreMensual procesarCierreMes(int mes, int anio) {
        // 1. Rango de fechas
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDateTime inicio = primerDia.atStartOfDay();
        LocalDateTime fin = primerDia.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        // 2. Ventas por método de pago (Flujo de Caja)
        BigDecimal efectivo = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "EFECTIVO"));
        BigDecimal tarjeta = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "TARJETA"));
        BigDecimal transferencia = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "TRANSFERENCIA"));
        BigDecimal mixto = nvl(ventaRepo.sumaPorMetodoPago(inicio, fin, "MIXTO"));
        BigDecimal TotalSalientePedidos = pedidoRepo.sumaTotalPedidosPorEstado(inicio, fin, EstadoPedido.ENTREGADO);
        BigDecimal totalRecaudado = efectivo.add(tarjeta).add(transferencia).add(mixto).add(TotalSalientePedidos);

        // 3. Impuestos (Pasivo)
        BigDecimal impVentas = nvl(ventaRepo.sumaImpuestosMes(inicio, fin));
        BigDecimal impPedidos = nvl(pedidoRepo.sumaImpuestosPedidos(inicio, fin));
        BigDecimal impuestosTotales = impVentas.add(impPedidos);

        // 4. Costos y Gastos (Egresos)
        BigDecimal totalEgresos = nvl(egresoRepo.sumarEgresosPorDia(inicio, fin));
        BigDecimal totalCompras = nvl(comprasRepo.sumTotalCompras(inicio, fin));

        // AQUÍ LA CORRECCIÓN: Costo de lo que se vendió para calcular utilidad
        BigDecimal costoVendido = nvl(productoRepo.sumaCostoVendidoMes(inicio, fin));

        // Costo de productos que salieron por pedidos confirmados
        BigDecimal costoPedidos = nvl(productoRepo.sumaCostoPedidosMes(inicio, fin, EstadoPedido.ENTREGADO));

        // Suma total de lo que te costó la mercancía que ya no está en stock
        BigDecimal costoTotalSaliente = costoVendido.add(costoPedidos);
        // 5. Valor del Inventario (Capital en Stock)
        // Usamos la consulta optimizada que te sugerí
        // 5. Valor del Inventario (Capital en Stock)
        // Usamos la consulta optimizada que te sugerí
        BigDecimal valorInventario = nvl(productoRepo.calcularValorInventarioTotal());

        // 6. CÁLCULO DE UTILIDADES CORRECTO
        // Ingresos reales (lo que entró menos el IVA cobrado)
        BigDecimal ventasNetas = totalRecaudado.subtract(impuestosTotales);

        // Utilidad Bruta: Margen después de pagar la mercancía
        BigDecimal utilidadBruta = ventasNetas.subtract(costoVendido);

        // Utilidad Neta: Lo que queda en tu bolsillo (Utilidad Bruta - Gastos)
        BigDecimal utilidadNeta = utilidadBruta.subtract(totalEgresos);

        BigDecimal TotalVentasPorMayor=nvl(ventaRepo.VentasTotalesAlMayor());
        // 7. Evitar duplicados
        // 7. Evitar duplicados
        cierreRepo.deleteByMesAndAnio(mes, anio);
        
        // 8. Construir objeto de cierre
        CierreMensual cierre = new CierreMensual();
        cierre.setMes(mes);
        cierre.setAnio(anio);
        cierre.setFechaCierre(LocalDate.now());
        cierre.setTotalVentasEfectivo(efectivo);
        cierre.setTotalVentasTarjeta(tarjeta);
        cierre.setTotalVentasTransferencia(transferencia);
        cierre.setRecaudacionTotal(totalRecaudado);
        cierre.setRecaudacionBruta(ventasNetas);
        cierre.setTotalImpuestos(impuestosTotales);
        cierre.setTotalEgresos(totalEgresos);
        cierre.setTotalCompras(totalCompras);
        cierre.setUtilidadBruta(utilidadBruta);
        cierre.setUtilidadNeta(utilidadNeta);
        cierre.setTotalCompras(totalCompras);
        cierre.setUtilidadBruta(utilidadBruta);
        cierre.setUtilidadNeta(utilidadNeta);
        // La empresa se setea automáticamente por TenantEntityListener

        // Estadísticas adicionales
        // Estadísticas adicionales
        cierre.setCantidadPedidos(pedidoRepo.cantidadPedidosPorRango(inicio, fin, EstadoPedido.ENTREGADO).intValue());
        cierre.setNuevosClientes(nvlInt(clienteRepo.contarNuevosClientesPorRango(inicio, fin)));
        cierre.setTotalProductosEnStock(productoRepo.findAll().size());
        cierre.setValorInventarioTotal(valorInventario);
        cierre.setTotalVentasAlMayor(TotalVentasPorMayor);

        return cierreRepo.save(cierre);
    }

    // Métodos preventivos para evitar que el programa falle si hay valores null
    private BigDecimal nvl(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

    private Integer nvlInt(Integer valor) {
        return valor == null ? 0 : valor;
    }

    @Override
    public Map<String, Object> obtenerResumenProyectado(int mes, int anio) {
        
        Map<String, Object> resumen = new HashMap<>();
        LocalDateTime fechaInicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fechaFin = fechaInicio.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        BigDecimal utilidadActual = calcularUtilidad(mes, anio);

        int mesAnt = (mes == 1) ? 12 : mes - 1;
        int anioAnt = (mes == 1) ? anio - 1 : anio;
        BigDecimal utilidadAnterior = calcularUtilidad(mesAnt, anioAnt);

        boolean utilidadSubio = utilidadActual.compareTo(utilidadAnterior) > 0;
        double porcentaje = 0.0;

        if (utilidadAnterior.compareTo(BigDecimal.ZERO) != 0) {
            porcentaje = utilidadActual.subtract(utilidadAnterior)
                    .divide(utilidadAnterior.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).doubleValue();
        }

        // 1. Inventarios
        long stockTotalUnd = productoRepo.findAll().stream().filter(p -> p.getTipoVenta().getCode()
                        .equalsIgnoreCase("94"))
                .mapToLong(p -> p.getCantidad().longValue()).sum();

        BigDecimal stockTotalkg = productoRepo.findAll().stream().filter(p -> p.getTipoVenta().getCode()
                        .equalsIgnoreCase("KGM"))
                .map(Productos::getCantidad).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorInventario = nvl(productoRepo.calcularValorInventarioTotal());

        // 2. Ventas por método de pago
        BigDecimal efectivo = nvl(ventaRepo.sumaPorMetodoPago(fechaInicio, fechaFin, "EFECTIVO"));
        BigDecimal tarjeta = nvl(ventaRepo.sumaPorMetodoPago(fechaInicio, fechaFin, "TARJETA"));
        BigDecimal transferencia = nvl(ventaRepo.sumaPorMetodoPago(fechaInicio, fechaFin, "TRANSFERENCIA"));
        BigDecimal mixto = nvl(ventaRepo.sumaPorMetodoPago(fechaInicio, fechaFin, "MIXTO"));

        // totalVentas representa solo ventas directas
        BigDecimal totalVentas = efectivo.add(tarjeta).add(transferencia).add(mixto);

        // 3. Consolidación de Pedidos (MOVIDO HACIA ARRIBA PARA EL CÁLCULO)
        BigDecimal TotalPediosConfirmados = pedidoRepo.sumaTotalPedidosPorEstado(fechaInicio, fechaFin, EstadoPedido.ENTREGADO);
        Long TotalPedidos = pedidoRepo.cantidadPedidosPorRango(fechaInicio, fechaFin, EstadoPedido.ENTREGADO);
        if (TotalPedidos == null) TotalPedidos = 0L;


        // Usamos esta suma para el Margen y el Ticket
        BigDecimal TotalIngresos = totalVentas.add(TotalPediosConfirmados).setScale(2, RoundingMode.HALF_UP);

        // 4. Margen Neto (Utilidad Neta / Ingresos Totales * 100)
        BigDecimal MargenNeto = BigDecimal.ZERO;
        if (TotalIngresos.compareTo(BigDecimal.ZERO) > 0) {
            // CORRECCIÓN: El margen se calcula sobre la utilidad final
            MargenNeto = utilidadActual.multiply(new BigDecimal("100"))
                    .divide(TotalIngresos, 2, RoundingMode.HALF_UP);
        }

        // 5. Consolidación de Cantidades y Ticket Promedio
        BigDecimal CantidadDeVentas = nvl(ventaRepo.CantidadDeVentas(fechaInicio, fechaFin));
        BigDecimal TotalCantidades = CantidadDeVentas.add(BigDecimal.valueOf(TotalPedidos));

        BigDecimal TicketPromedio = BigDecimal.ZERO.setScale(2);
        if (TotalCantidades.compareTo(BigDecimal.ZERO) > 0) {
            // El ticket promedio debe ser sobre el dinero TOTAL
            TicketPromedio = TotalIngresos.divide(TotalCantidades, 2, RoundingMode.HALF_UP);
        }
        BigDecimal TotalVentasAlMAyor = nvl(ventaRepo.VentasTotalesAlMayor());
        BigDecimal TotalVentasAlDetal = nvl(TotalIngresos.subtract(TotalVentasAlMAyor));

        BigDecimal egresos = nvl(egresoRepo.sumarEgresosPorDia(fechaInicio, fechaFin));

        // Mantenemos tus nombres exactos en el mapa
        resumen.put("stockUnidades", stockTotalUnd);
        resumen.put("stockPesable", stockTotalkg);
        resumen.put("inversion", valorInventario);
        resumen.put("gastos", egresos);
        resumen.put("utilidad", utilidadActual);
        resumen.put("utilidadSubio", utilidadSubio);
        resumen.put("porcentajeUtilidad", Math.abs(porcentaje));
        resumen.put("ingresosTotales", TotalIngresos); 
        resumen.put("porcentajeMargen", MargenNeto); // Enviamos el Margen Neto calculado
        resumen.put("ticketPromedio", TicketPromedio);
        resumen.put("totalPedidos", TotalPedidos);
        resumen.put("VentasDetal",TotalVentasAlDetal);
        resumen.put("VentasMayor", TotalVentasAlMAyor);

        return resumen;
    }

    @Override
    public BigDecimal calcularUtilidad(int mes, int anio) {
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        // 1. OBTENER DINERO TOTAL ENTRADO (Con IVA)
        BigDecimal ingresosBrutosVentas = nvl(ventaRepo.sumaVentasRango(inicio, fin));
        BigDecimal ingresosBrutosPedidos = nvl(pedidoRepo.sumaTotalPedidosPorEstado(inicio, fin, EstadoPedido.ENTREGADO));
        BigDecimal granTotalEntradas = ingresosBrutosVentas.add(ingresosBrutosPedidos);

        // 2. OBTENER IVA (Este dinero no es tuyo, se lo debes al estado)
        BigDecimal totalIva = nvl(ventaRepo.sumaImpuestosMes(inicio, fin))
                .add(nvl(pedidoRepo.sumaImpuestosPedidos(inicio, fin)));

        // 3. VENTAS NETAS (Lo que realmente es del negocio antes de costos)
        BigDecimal ventasNetasReales = granTotalEntradas.subtract(totalIva);

        // 4. COSTO DE LO VENDIDO (Lo que pagaste por lo que salió)
        BigDecimal costoMercancia = nvl(productoRepo.sumaCostoVendidoMes(inicio, fin))
                .add(nvl(productoRepo.sumaCostoPedidosMes(inicio, fin, EstadoPedido.ENTREGADO)));

        // 5. UTILIDAD BRUTA (Ventas Netas - Costo de Mercancía)
        // Si esto es positivo, el negocio es rentable por producto.
        BigDecimal utilidadBruta = ventasNetasReales.subtract(costoMercancia);

        // 6. GASTOS FIJOS (Egresos)
        BigDecimal egresos = nvl(egresoRepo.sumarEgresosPorDia(inicio, fin));

        // 7. UTILIDAD NETA (Lo que queda en tu bolsillo)
        return utilidadBruta.subtract(egresos).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public CierreMensual obtenerPorId(Long id) {
        return cierreRepo.findById(id).orElseThrow(()->new RuntimeException("categoria no encontrada"));
    }
}
