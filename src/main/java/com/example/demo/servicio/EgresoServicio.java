package com.example.demo.servicio;

import com.example.demo.entidad.Egresos;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EgresoServicio {

    void CrearGasto(Egresos egresos);
    List<Egresos> ListarGastos();
    Map<String,Object>DatosEgresos(LocalDateTime incio, LocalDateTime fin);
    void deleteGasto(Long id);
    Egresos ObtenerEgreso(Long id );
    void UpdateEgreso(Egresos egresos);
}
