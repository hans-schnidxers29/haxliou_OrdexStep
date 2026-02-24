package com.example.demo.servicio;
import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.CierreMensual;
import com.example.demo.entidad.Empresa;
import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.entidad.Enum.MetodoPago;
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
    @Autowired private SecurityService securityService;



    @Override
    @Transactional
    public CierreMensual procesarCierreMes(int mes, int anio) {

        Empresa empresa = securityService.ObtenerEmpresa();
        Long empresaId = empresa.getId();
        // 1. Rango de fechas
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDateTime inicio = primerDia.atStartOfDay();
        LocalDateTime fin = primerDia.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        Map<String,Object> datosDeResumen = obtenerResumenProyectado(mes,anio);

        // 2. Ventas por método de pago (Flujo de Caja)
        BigDecimal efectivo = (BigDecimal) datosDeResumen.get("efectivo");
        BigDecimal tarjeta = (BigDecimal) datosDeResumen.get("tarjeta");
        BigDecimal transferencia = (BigDecimal) datosDeResumen.get("transferencia");

        BigDecimal TotalSalientePedidos = pedidoRepo.sumaTotalPedidosPorEstado(inicio, fin, EstadoPedido.ENTREGADO,empresaId);
        BigDecimal totalRecaudado = efectivo.add(tarjeta).add(transferencia).add(TotalSalientePedidos);

        // 3. Impuestos (Pasivo)
        BigDecimal impVentas = nvl(ventaRepo.sumaImpuestosMes(inicio, fin,empresaId));
        BigDecimal impPedidos = nvl(pedidoRepo.sumaImpuestosPedidos(inicio, fin,empresaId));
        BigDecimal impuestosTotales = impVentas.add(impPedidos);

        // 4. Costos y Gastos (Egresos)
        BigDecimal totalEgresos = nvl(egresoRepo.sumarEgresosPorDia(inicio, fin,empresaId));
        BigDecimal totalComprasEncredito= nvl(comprasRepo.comprasPorCreditos(empresaId,inicio,fin));
        BigDecimal totalCompras = nvl(comprasRepo.sumTotalCompras(inicio, fin ,empresaId).add(totalComprasEncredito));

        // AQUÍ LA CORRECCIÓN: Costo de lo que se vendió para calcular utilidad
        BigDecimal costoVendido = nvl(productoRepo.sumaCostoVendidoMes(inicio, fin,empresaId));

        // Costo de productos que salieron por pedidos confirmados
        BigDecimal costoPedidos = nvl(productoRepo.sumaCostoPedidosMes(inicio, fin, EstadoPedido.ENTREGADO,empresaId));

        // Suma total de lo que te costó la mercancía que ya no está en stock
        BigDecimal costoTotalSaliente = costoVendido.add(costoPedidos);
        // 5. Valor del Inventario (Capital en Stock)
        // Usamos la consulta optimizada que te sugerí
        BigDecimal valorInventario = nvl(productoRepo.calcularValorInventarioTotal(empresaId));

        // 6. CÁLCULO DE UTILIDADES CORRECTO
        // Ingresos reales (lo que entró menos el IVA cobrado)
        BigDecimal ventasNetas = totalRecaudado.subtract(impuestosTotales);

        // Utilidad Bruta: Margen después de pagar la mercancía
        BigDecimal utilidadBruta = ventasNetas.subtract(costoVendido);

        // Utilidad Neta: Lo que queda después de pagar arriendo, servicios, etc. (Egresos)
        BigDecimal utilidadNeta = utilidadBruta.subtract(totalEgresos);

        BigDecimal TotalVentasPorMayor=nvl(ventaRepo.VentasTotalesAlMayor(empresaId));
        // 7. Evitar duplicados
        cierreRepo.eliminarCierreExistente(mes, anio,empresaId);
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
        cierre.setEmpresa(empresa);

        // Estadísticas adicionales
        cierre.setCantidadPedidos(pedidoRepo.cantidadPedidosPorRango(inicio, fin, EstadoPedido.ENTREGADO,empresaId).intValue());
        cierre.setNuevosClientes(nvlInt(clienteRepo.contarNuevosClientesPorRango(inicio, fin,empresaId)));
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
        Long empresaId = securityService.obtenerEmpresaId();

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

        BigDecimal valorInventario = nvl(productoRepo.calcularValorInventarioTotal(empresaId));
        BigDecimal efectivoMixto = nvl(ventaRepo.ValoresPorVentasMixtas(fechaInicio,fechaFin, MetodoPago.EFECTIVO,empresaId));
        BigDecimal transfMixto = nvl(ventaRepo.ValoresPorVentasMixtas(fechaInicio,fechaFin, MetodoPago.TRANFERENCIA,empresaId));
        BigDecimal tarjetMixto = nvl(ventaRepo.ValoresPorVentasMixtas(fechaInicio,fechaFin, MetodoPago.TARJETA,empresaId));

        // 2. Ventas por método de pago
        BigDecimal efectivo = nvl(ventaRepo.sumaPorMetodoPago(fechaInicio, fechaFin, "EFECTIVO",empresaId).
                add(efectivoMixto));
        BigDecimal tarjeta = nvl(ventaRepo.sumaPorMetodoPago(fechaInicio, fechaFin, "TARJETA",empresaId).
                add(tarjetMixto));
        BigDecimal transferencia = nvl(ventaRepo.sumaPorMetodoPago(fechaInicio, fechaFin, "TRANFERENCIA",empresaId).
                add(transfMixto));

        // totalVentas representa solo ventas directas
        BigDecimal totalVentas = efectivo.add(tarjeta).add(transferencia);

        // 3. Consolidación de Pedidos (MOVIDO HACIA ARRIBA PARA EL CÁLCULO)
        BigDecimal TotalPediosConfirmados = pedidoRepo.sumaTotalPedidosPorEstado(fechaInicio, fechaFin, EstadoPedido.ENTREGADO,empresaId);
        Long TotalPedidos = pedidoRepo.cantidadPedidosPorRango(fechaInicio, fechaFin, EstadoPedido.ENTREGADO,empresaId);
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
        BigDecimal CantidadDeVentas = nvl(ventaRepo.CantidadDeVentas(fechaInicio, fechaFin,empresaId));
        BigDecimal TotalCantidades = CantidadDeVentas.add(BigDecimal.valueOf(TotalPedidos));

        BigDecimal TicketPromedio = BigDecimal.ZERO.setScale(2);
        if (TotalCantidades.compareTo(BigDecimal.ZERO) > 0) {
            // El ticket promedio debe ser sobre el dinero TOTAL
            TicketPromedio = TotalIngresos.divide(TotalCantidades, 2, RoundingMode.HALF_UP);
        }
        BigDecimal TotalVentasAlMAyor = nvl(ventaRepo.VentasTotalesAlMayor(empresaId));
        BigDecimal TotalVentasAlDetal = nvl(TotalIngresos.subtract(TotalVentasAlMAyor));

        BigDecimal egresos = nvl(egresoRepo.sumarEgresosPorDia(fechaInicio, fechaFin,empresaId));

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
        resumen.put("efectivo",efectivo);
        resumen.put("tarjeta",tarjeta);
        resumen.put("transferencia",transferencia);

        return resumen;
    }

    @Override
    public BigDecimal calcularUtilidad(int mes, int anio) {
        Long empresaId = securityService.obtenerEmpresaId();
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);

        // 1. OBTENER DINERO TOTAL ENTRADO (Con IVA)
        BigDecimal ingresosBrutosVentas = nvl(ventaRepo.sumaVentasRango(inicio, fin,empresaId));
        BigDecimal ingresosBrutosPedidos = nvl(pedidoRepo.sumaTotalPedidosPorEstado(inicio, fin, EstadoPedido.ENTREGADO,empresaId));
        BigDecimal granTotalEntradas = ingresosBrutosVentas.add(ingresosBrutosPedidos);

        // 2. OBTENER IVA (Este dinero no es tuyo, se lo debes al estado)
        BigDecimal totalIva = nvl(ventaRepo.sumaImpuestosMes(inicio, fin,empresaId))
                .add(nvl(pedidoRepo.sumaImpuestosPedidos(inicio, fin,empresaId)));

        // 3. VENTAS NETAS (Lo que realmente es del negocio antes de costos)
        BigDecimal ventasNetasReales = granTotalEntradas.subtract(totalIva);

        // 4. COSTO DE LO VENDIDO (Lo que pagaste por lo que salió)
        BigDecimal costoMercancia = nvl(productoRepo.sumaCostoVendidoMes(inicio, fin,empresaId))
                .add(nvl(productoRepo.sumaCostoPedidosMes(inicio, fin, EstadoPedido.ENTREGADO,empresaId)));

        // 5. UTILIDAD BRUTA (Ventas Netas - Costo de Mercancía)
        // Si esto es positivo, el negocio es rentable por producto.
        BigDecimal utilidadBruta = ventasNetasReales.subtract(costoMercancia);

        // 6. GASTOS FIJOS (Egresos)
        BigDecimal egresos = nvl(egresoRepo.sumarEgresosPorDia(inicio, fin,empresaId));

        // 7. UTILIDAD NETA (Lo que queda en tu bolsillo)
        return utilidadBruta.subtract(egresos).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public CierreMensual obtenerPorId(Long id) {
        return cierreRepo.findById(id).orElseThrow(()->new RuntimeException("categoria no encontrada"));
    }
}
