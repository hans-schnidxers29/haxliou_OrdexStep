package com.example.demo.servicio;

import com.example.demo.entidad.CierreMensual;

import java.math.BigDecimal;
import java.util.Map;

public interface CierreMensualServicio {

    CierreMensual procesarCierreMes(int mes, int anio);
    Map<String, Object> obtenerResumenProyectado(int mes, int anio);
    BigDecimal calcularUtilidad(int mes, int anio);
}
