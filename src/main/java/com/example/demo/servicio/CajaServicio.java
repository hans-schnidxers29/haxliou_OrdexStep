package com.example.demo.servicio;

import com.example.demo.entidad.Usuario;
import com.example.demo.entidad.Caja;

import java.math.BigDecimal;
import java.util.Map;

public interface CajaServicio {
    Caja CajaAbierta(Long EmpresaId);
    Caja CerrarCaja(Long id,BigDecimal MontoEnCaja);
    void EjecutarCaja(Usuario user, BigDecimal MontoInicial);
    Map<String ,Object> obtenerResumenActual(Long cajaId);
    Caja cajaByid(Long id);
}
