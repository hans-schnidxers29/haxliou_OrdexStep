package com.example.demo.servicio;

import com.example.demo.entidad.Enum.MetodoPago;

import java.math.BigDecimal;

public interface FinanzasServicio {

    void ProcesarAbono(Long cuentaId,
                       BigDecimal monto, MetodoPago metodoPago, boolean afectoCaja, BigDecimal montoEfec, BigDecimal montoTrns) throws IllegalAccessException;
}
