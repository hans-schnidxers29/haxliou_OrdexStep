package com.example.demo.servicio;

import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.Empresa;
import com.example.demo.entidad.Enum.MetodoPago;
import com.example.demo.entidad.Usuario;
import com.example.demo.repositorio.*;
import com.example.demo.entidad.Caja;
import com.example.demo.entidad.Enum.EstadoDeCaja;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class CajaServicioImp implements CajaServicio{

    @Autowired
    private Cajarepositorio cajaRepositorio;

    @Autowired
    private EgresoRepositorio egresoRepositorio;

    @Autowired
    private ComprasRepositorio comprasRepositorio;

    @Autowired
    private VentaRepositorio ventarepositorio;

    @Autowired
    private SecurityService securityService;
    
    @Autowired
    private FinanzasRepositorio finanzasRepositorio;


    @Override
    public Caja CajaAbierta(Long  EmpresaId) {
        return cajaRepositorio.findByUsuarioAndEstadoAndEmpresaId(EstadoDeCaja.EN_PROCESO,securityService.obtenerEmpresaId()).orElse(null);
    }

    @Transactional
    @Override
    public Caja CerrarCaja(Long id, BigDecimal montoEnCaja) {

        Caja caja = cajaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        if (caja.getEstado() == EstadoDeCaja.CERRADA) {
            return caja; // Si ya está cerrada, solo la devolvemos para el PDF
        }

        LocalDateTime inicio = caja.getFechaApertura();
        LocalDateTime fin = LocalDateTime.now();
        // Cálculos (Tus consultas actuales)
        Map<String, Object> resumen = obtenerResumenActual(caja.getId());
        BigDecimal totalEgresos = (BigDecimal) resumen.get("egresosGastos");
        BigDecimal totalCompras =  (BigDecimal) resumen.get("egresosCompras");;
        BigDecimal totalVentas = (BigDecimal) resumen.get("totalVentas");

        BigDecimal saldoTeorico = caja.getMontoInicial().add(totalVentas).subtract(totalEgresos).subtract(totalCompras);

        // Actualización de la entidad
        caja.setIngresoTotal(totalVentas);
        caja.setEgresosTotales(totalEgresos);
        caja.setGastosTotales(totalCompras);
        caja.setMontoReal(montoEnCaja);
        caja.setDiferencia(montoEnCaja.subtract(saldoTeorico));
        caja.setFechaCierre(fin);
        caja.setEstado(EstadoDeCaja.CERRADA);

         return cajaRepositorio.save(caja);
    }


    @Override
    public void EjecutarCaja(Usuario user, BigDecimal MontoInicial) {

        Empresa empresa = securityService.ObtenerEmpresa();
        Long empresaId = empresa.getId();
        boolean CajaEnEjecucion = cajaRepositorio.existsByUsuarioAndEstadoAndEmpresaId(user, EstadoDeCaja.EN_PROCESO,empresaId);
        if (CajaEnEjecucion) {
            throw new IllegalStateException("Ya existe una caja abierta para este usuario" + user.getEmail());
        }

        if(empresa == null){
            throw new IllegalStateException("error al abrir caja");
        }

        Caja abrirCaja = new Caja();
        abrirCaja.setMontoInicial(MontoInicial);
        abrirCaja.setUsuario(user);
        abrirCaja.setFechaApertura(LocalDateTime.now());
        abrirCaja.setEstado(EstadoDeCaja.EN_PROCESO);
        abrirCaja.setEmpresa(empresa);
        cajaRepositorio.save(abrirCaja);
    }

    @Override
    public Map<String, Object> obtenerResumenActual(Long cajaId) {

        Caja caja = cajaRepositorio.findById(cajaId)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        LocalDateTime inicio = caja.getFechaApertura();
        LocalDateTime fin = LocalDateTime.now();

        Long empresaId =securityService.obtenerEmpresaId();
        // Protección contra nulos
        BigDecimal egresos = nvl(egresoRepositorio.egresosSalioCaja(inicio, fin,
                Arrays.asList(MetodoPago.EFECTIVO,MetodoPago.MIXTO),empresaId));
        BigDecimal compras = nvl(finanzasRepositorio.sumarAbonosCompraBySalioCajaEfectivo(inicio, fin, empresaId,
                Arrays.asList(MetodoPago.MIXTO, MetodoPago.EFECTIVO)));

        //Valores en metodo Mixto
        BigDecimal EfectivoMixto = nvl(ventarepositorio.ValoresPorVentasMixtas(inicio, fin, MetodoPago.EFECTIVO,
                empresaId));
        BigDecimal TarjetaMixto = nvl(ventarepositorio.ValoresPorVentasMixtas(inicio, fin, MetodoPago.TARJETA,
                empresaId));
        BigDecimal TranferenciaMixto = nvl(ventarepositorio.ValoresPorVentasMixtas(inicio, fin, MetodoPago.TRANFERENCIA,
                empresaId));


        // Obtención de ventas por método
        BigDecimal efectivo = nvl(ventarepositorio.sumaPorMetodoPago(inicio, fin, "EFECTIVO",
                empresaId).add(EfectivoMixto));
        BigDecimal tarjeta = nvl(ventarepositorio.sumaPorMetodoPago(inicio, fin, "TARJETA",
                empresaId).add(TarjetaMixto));
        BigDecimal transferencia = nvl(ventarepositorio.sumaPorMetodoPago(inicio, fin, "TRANFERENCIA",
                empresaId).add(TranferenciaMixto));

        // El saldo en CAJA FÍSICA solo debe sumar el EFECTIVO (y quizás una parte del mixto)
        // Aquí corregimos la duplicidad que tenías:
        BigDecimal saldoActual = caja.getMontoInicial()
                .add(efectivo)
                .subtract(egresos)
                .subtract(compras);

        // Total de todas las ventas (para estadística)
        BigDecimal totalVentas = efectivo.add(tarjeta).add(transferencia);

        Map<String, Object> resumenCaja = new HashMap<>();
        resumenCaja.put("montoInicial", caja.getMontoInicial());
        resumenCaja.put("ingresosEfectivo", efectivo);
        resumenCaja.put("egresosGastos", egresos);
        resumenCaja.put("egresosCompras", compras);
        resumenCaja.put("egresosTotales", egresos.add(compras));
        resumenCaja.put("saldoActual", saldoActual);
        resumenCaja.put("totalVentas", totalVentas);
        resumenCaja.put("ventasTarjeta", tarjeta);
        resumenCaja.put("ventasTransferencia", transferencia);
        resumenCaja.put("fechaConsulta", fin);
        resumenCaja.put("fechaApertura", caja.getFechaApertura());

        return resumenCaja;
    }

    private BigDecimal nvl(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Override
    public Caja cajaByid(Long id) {
        return cajaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Caja no encontrada"));
    }
}
