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
    public void CerrarCaja(Long id, BigDecimal MontoEncaja) {
       try {
           Caja cerrarCaja = cajaRepositorio.findById(id).orElseThrow(
                   () -> new RuntimeException("Caja no encontrada"));

           if (cerrarCaja.getEstado() == EstadoDeCaja.CERRADA) {
               throw new IllegalStateException("Caja ya fue Cerrada");
           }
           LocalDateTime inicio = cerrarCaja.getFechaApertura();
           LocalDateTime fin = LocalDate.now().atTime(LocalTime.MAX);

           BigDecimal TotalEgresos = egresoRepositorio.sumarEgresosPorDia(inicio, fin);
           BigDecimal Totalcomras = comprasRepositorio.sumaEgresosHoy(inicio, fin);
           BigDecimal TotalVentas = ventarepositorio.sumaVentasRango(inicio, fin);

           BigDecimal SaldoTeorico = cerrarCaja.getMontoInicial()
                   .add(TotalVentas)
                   .subtract(TotalEgresos)
                   .subtract(Totalcomras);
           cerrarCaja.setMontoReal(MontoEncaja);
           cerrarCaja.setDiferencia(MontoEncaja.subtract(SaldoTeorico));

           cerrarCaja.setEgresosTotales(TotalEgresos);
           cerrarCaja.setGastosTotales(Totalcomras);
           cerrarCaja.setIngresoTotal(TotalVentas);
           cerrarCaja.setFechaCierre(LocalDateTime.now());
           cerrarCaja.setEstado(EstadoDeCaja.CERRADA);
       }catch (DataAccessException e){
           throw new RuntimeException("Error al cerrar caja en la nube" + e.getMessage());
       }catch (Exception e){
           throw new RuntimeException("Error al cerrar caja" + e.getMessage());
       }

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
    public Caja obtenerResumenActual(Long cajaId) {
        Caja caja = cajaRepositorio.findById(cajaId)
                .orElseThrow(() -> new RuntimeException("Caja no encontrada"));

        if(caja.getEstado()== EstadoDeCaja.CERRADA){
            throw new IllegalStateException("Caja ya fue cerrada");
        }

        LocalDateTime inicio = caja.getFechaApertura();
        LocalDateTime fin = LocalDateTime.now();


        BigDecimal egresos = egresoRepositorio.sumarEgresosPorDia(inicio, fin);
        BigDecimal compras = comprasRepositorio.sumaEgresosHoy(inicio, fin);
        BigDecimal ventas = ventarepositorio.sumaVentasRango(inicio, fin);


        caja.setEgresosTotales(egresos);
        caja.setGastosTotales(compras);
        caja.setIngresoTotal(ventas);


        BigDecimal saldoActual = caja.getMontoInicial()
                .add(ventas)
                .subtract(egresos)
                .subtract(compras);


        caja.setMontoReal(saldoActual);

        return caja;
    }

    @Override
    public Caja cajaByid(Long id) {
        return cajaRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Caja no encontrada"));
    }
}
