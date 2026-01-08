package com.example.demo.servicio;

import com.example.demo.entidad.Egresos;

import java.util.List;

public interface EgresoServicio {

    void CrearGasto(Egresos egresos);
    List<Egresos> ListarGastos();
}
