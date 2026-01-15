package com.example.demo.controlador;

import com.example.demo.entidad.CierreMensual;
import com.example.demo.servicio.CierreMensualServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/cierre")
public class CierreMensualControlador {

    @Autowired
    private CierreMensualServicio servicio;

    @GetMapping("/resumen")
    public String tuMes(@RequestParam(required = false) Integer mes,
                        @RequestParam(required = false) Integer anio,
                        Model model) {
        if (mes == null) mes = LocalDate.now().getMonthValue();
        if (anio == null) anio = LocalDate.now().getYear();

        Map<String, Object> datos = servicio.obtenerResumenProyectado(mes, anio);
        model.addAttribute("datos", datos);
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        return "viewCierre/resumenDeMes";
    }
}
