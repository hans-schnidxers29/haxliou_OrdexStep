package com.example.demo.controlador;

import com.example.demo.ModuloVentas.VentaServicio;
import com.example.demo.entidad.CierreMensual;
import com.example.demo.servicio.CierreMensualServicio;
import com.example.demo.servicio.ClienteService;
import com.example.demo.servicio.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cierre")
public class CierreMensualControlador {

    @Autowired
    private CierreMensualServicio servicio;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio servicioVentas;

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
        model.addAttribute("montosClientes",clienteService.CantidadPedidosPorPersonas().get("cantidades"));
        model.addAttribute("nombresClientes",clienteService.CantidadPedidosPorPersonas().get("nombres"));
        model.addAttribute("productosNombres", productoServicio.NombreProductosVentas());
        model.addAttribute("productosCantidades", productoServicio.CantidadProductosVentas());
        List<String> etiquetas = servicioVentas.ListaMeses();
        List<BigDecimal> valores = servicioVentas.listarTotalVentas();
        model.addAttribute("labelsGrafica", etiquetas);
        model.addAttribute("datosGrafica", valores );
        return "viewCierre/resumenDeMes";
    }

    @GetMapping("/mes")
    public String mes(){

        return "viewCierre/resumenDeMes";
    }
}
