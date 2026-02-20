package com.example.demo.servicio;

import com.example.demo.entidad.Enum.MetodoPago;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FinanzasServicioImp  implements FinanzasServicio{

    @Override
    public void ProcesarAbono(Long cuentaId, BigDecimal monto, MetodoPago metodoPago, boolean afectoCaja) {

    }
}
