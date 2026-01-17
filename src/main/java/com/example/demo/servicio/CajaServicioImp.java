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
    public void CerrarCaja(Long id, BigDecimal montoEnCaja) {

        Caja cerrarCaja = cajaRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        if (cerrarCaja.getEstado() == EstadoDeCaja.CERRADA) {
            throw new IllegalStateException("Caja ya fue cerrada");
        }

        LocalDateTime inicio = cerrarCaja.getFechaApertura();
        LocalDateTime fin = LocalDateTime.now();

        BigDecimal totalEgresos = Optional.ofNullable(
                egresoRepositorio.sumarEgresosPorDia(inicio, fin)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalCompras = Optional.ofNullable(
                comprasRepositorio.sumTotalCompras(inicio, fin)
        ).orElse(BigDecimal.ZERO);

        BigDecimal totalVentas = Optional.ofNullable(
                ventarepositorio.sumaVentasRango(inicio, fin)
        ).orElse(BigDecimal.ZERO);

        BigDecimal saldoTeorico = cerrarCaja.getMontoInicial()
                .add(totalVentas)
                .subtract(totalEgresos)
                .subtract(totalCompras);

        cerrarCaja.setIngresoTotal(totalVentas);
        cerrarCaja.setEgresosTotales(totalEgresos);
        cerrarCaja.setGastosTotales(totalCompras);
        cerrarCaja.setMontoReal(montoEnCaja);
        cerrarCaja.setDiferencia(montoEnCaja.subtract(saldoTeorico));
        cerrarCaja.setFechaCierre(LocalDateTime.now());
        cerrarCaja.setEstado(EstadoDeCaja.CERRADA);

        cajaRepositorio.save(cerrarCaja);
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

        if (caja.getEstado() == EstadoDeCaja.CERRADA) {
            throw new IllegalStateException("La caja seleccionada ya está cerrada.");
        }

        LocalDateTime inicio = caja.getFechaApertura();
        LocalDateTime fin = LocalDateTime.now();

        // Protección contra nulos (Null Safe)
        BigDecimal egresos = Optional.ofNullable(egresoRepositorio.sumarEgresosPorDia(inicio, fin))
                .orElse(BigDecimal.ZERO);

        BigDecimal compras = Optional.ofNullable(comprasRepositorio.sumTotalCompras(inicio, fin))
                .orElse(BigDecimal.ZERO);

        BigDecimal ventasEfectivo = Optional.ofNullable(ventarepositorio.sumaPorMetodoPago(inicio, fin, "EFECTIVO"))
                .orElse(BigDecimal.ZERO);

        // Cálculo del saldo esperado en caja física
        BigDecimal saldoActual = caja.getMontoInicial()
                .add(ventasEfectivo)
                .subtract(egresos)
                .subtract(compras);

        Map<String, Object> resumenCaja = new HashMap<>();
        resumenCaja.put("montoInicial", caja.getMontoInicial());
        resumenCaja.put("ingresosEfectivo", ventasEfectivo);
        resumenCaja.put("egresosTotales", egresos.add(compras)); // Suma de gastos y compras
        resumenCaja.put("saldoActual", saldoActual);
        resumenCaja.put("fechaConsulta", fin);

        return resumenCaja;
    }

    @Override
    public Caja cajaByid(Long id) {
        return cajaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Caja no encontrada"));
    }
}
