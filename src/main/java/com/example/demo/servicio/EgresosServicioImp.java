package com.example.demo.servicio;

import com.example.demo.entidad.Egresos;
import com.example.demo.entidad.Enum.TipoEgreso;
import com.example.demo.repositorio.EgresoRepositorio;
import org.hibernate.dialect.function.NvlCoalesceEmulation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EgresosServicioImp implements EgresoServicio{

    @Autowired
    private EgresoRepositorio repositorio;


    @Override
    public void CrearGasto(Egresos egresos) {
        repositorio.save(egresos);
    }

    @Override
    public List<Egresos> ListarGastos() {
        return repositorio.findAll().stream().toList();
    }

    @Override
    public Map<String, Object> DatosEgresos(LocalDateTime inicio, LocalDateTime fin) {

        BigDecimal egresosMensuales = repositorio.sumarEgresosPorDia(inicio,fin);
        BigDecimal GastosFijos = repositorio.SumaEgresosPorTipo(inicio, fin, TipoEgreso.GASTO_FIJO);
        BigDecimal GastosVariables = repositorio.SumaEgresosPorTipo(inicio,fin,TipoEgreso.GASTOS_VARIABLES);

        Map<String,Object> datos = new HashMap<>();
        // Lógica CORRECTA: Si es null, poner CERO. Si existe, usar el VALOR con escala 2.
        datos.put("EgresosTotales", (egresosMensuales == null) ? BigDecimal.ZERO.setScale(2) : egresosMensuales
                .setScale(2, RoundingMode.HALF_UP));
        datos.put("GastosFijos", (GastosFijos == null) ? BigDecimal.ZERO.setScale(2) : GastosFijos
                .setScale(2, RoundingMode.HALF_UP));
        datos.put("GastosVariables", (GastosVariables == null) ? BigDecimal.ZERO
                .setScale(2) : GastosVariables.setScale(2, RoundingMode.HALF_UP));

        return datos;
    }
}
