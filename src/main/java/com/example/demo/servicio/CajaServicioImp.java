package com.example.demo.servicio;

import com.example.demo.Login.Usuario;
import com.example.demo.ModuloVentas.VentaRepositorio;
import com.example.demo.entidad.Caja;
import com.example.demo.entidad.Enum.EstadoDeCaja;
import com.example.demo.repositorio.Cajarepositorio;
import com.example.demo.repositorio.ComprasRepositorio;
import com.example.demo.repositorio.EgresoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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


    @Override
    public Caja CajaAbierta(Usuario user) {
    return cajaRepositorio.findByUsuarioAndEstado(user,EstadoDeCaja.EN_PROCESO).orElse(null);
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
        BigDecimal totalEgresos = nvl(egresoRepositorio.sumarEgresosPorDia(inicio, fin));
        BigDecimal totalCompras = nvl(comprasRepositorio.sumTotalCompras(inicio, fin));
        BigDecimal totalVentas = nvl(ventarepositorio.sumaVentasRango(inicio, fin));

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

        boolean CajaEnEjecucion = cajaRepositorio.existsByUsuarioAndEstado(user, EstadoDeCaja.EN_PROCESO);
        if (CajaEnEjecucion) {
            throw new IllegalStateException("Ya existe una caja abierta para este usuario" + user.getEmail());
        }

        Caja abrirCaja = new Caja();
        abrirCaja.setMontoInicial(MontoInicial);
        abrirCaja.setUsuario(user);
        abrirCaja.setFechaApertura(LocalDateTime.now());
        abrirCaja.setEstado(EstadoDeCaja.EN_PROCESO);
        cajaRepositorio.save(abrirCaja);
    }

    @Override
    public Map<String, Object> obtenerResumenActual(Long cajaId) {

        Caja caja = cajaRepositorio.findById(cajaId)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        LocalDateTime inicio = caja.getFechaApertura();
        LocalDateTime fin = LocalDateTime.now();

        // Protección contra nulos (Null Safe)
        BigDecimal egresos = Optional.ofNullable(egresoRepositorio.sumarEgresosPorDia(inicio, fin))
                .orElse(BigDecimal.ZERO);

        BigDecimal compras = Optional.ofNullable(comprasRepositorio.sumTotalCompras(inicio, fin))
                .orElse(BigDecimal.ZERO);

        BigDecimal ventasEfectivo = Optional.ofNullable(ventarepositorio.sumaPorMetodoPago(inicio, fin, "EFECTIVO"))
                .orElse(BigDecimal.ZERO);
        BigDecimal efectivo = nvl(ventarepositorio.sumaPorMetodoPago(inicio, fin, "EFECTIVO"));
        BigDecimal tarjeta = nvl(ventarepositorio.sumaPorMetodoPago(inicio, fin, "TARJETA"));
        BigDecimal transferencia = nvl(ventarepositorio.sumaPorMetodoPago(inicio, fin, "TRANSFERENCIA"));
        BigDecimal mixto = nvl(ventarepositorio.sumaPorMetodoPago(inicio, fin, "MIXTO"));
        BigDecimal totalVentas = efectivo.add(tarjeta).add(transferencia).add(mixto);

        // Cálculo del saldo esperado en caja física
        BigDecimal saldoActual = caja.getMontoInicial()
                .add(ventasEfectivo)
                .add(efectivo)
                .subtract(egresos)
                .subtract(compras);

        Map<String, Object> resumenCaja = new HashMap<>();
        resumenCaja.put("montoInicial", caja.getMontoInicial());
        resumenCaja.put("ingresosEfectivo", ventasEfectivo);
        resumenCaja.put("egresosTotales", egresos.add(compras)); // Suma de gastos y compras
        resumenCaja.put("saldoActual", saldoActual);
        resumenCaja.put("fechaConsulta", fin);
        resumenCaja.put("ventasTarjeta", tarjeta);
        resumenCaja.put("ventasTransferencia", transferencia);
        resumenCaja.put("ventasMixto", mixto);
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
