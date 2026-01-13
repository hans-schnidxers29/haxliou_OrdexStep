package com.example.demo.servicio;

import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Caja;

import java.math.BigDecimal;

public interface CajaServicio {
    Caja CajaAbierta(Usuario user);
    void CerrarCaja(Long id,BigDecimal MontoEnCaja);
    void EjecutarCaja(Usuario user, BigDecimal MontoInicial);
    Caja obtenerResumenActual(Long cajaId);
    Caja cajaByid(Long id);
}
