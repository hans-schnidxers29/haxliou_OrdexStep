package com.example.demo.controlador;

import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Egresos;
import com.example.demo.servicio.EgresoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequestMapping("/egresos")
public class EgresosControlador {


    @Autowired
    private EgresoServicio egresoServicio;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @GetMapping("/crear")
    public String Mostrarformulario(Model model){
        Egresos egresos = new Egresos();
        model.addAttribute("egreso",egresos);
        return "viewEgresos/crearEgreso";
    }

    @PostMapping("/crear/nuevo")
    public String Crearegreso(@ModelAttribute("egreso") Egresos egresos, @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes){
        try {
            if (egresos.getMonto() == null) {
                redirectAttributes.addFlashAttribute("error", "El monto no puede ser vacio");
                return "redirect:/egresos/crear";
            }
            Usuario usuario_registra = servicioUsuario.findByEmail(userDetails.getUsername());
            if (usuario_registra == null) {
                redirectAttributes.addFlashAttribute("error", "Error al crear el egreso: Usuario no encontrado");
                return "redirect:/egresos/crear";
            }
            egresos.setUsuario(usuario_registra);
            egresoServicio.CrearGasto(egresos);
            redirectAttributes.addFlashAttribute("success", "Egreso creado exitosamente");
            return "redirect:/egresos/listar";

        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al crear el egreso: " + e.getMessage());
            return "redirect:/egresos/listar";
        }
    }

    @GetMapping("/listar")
    public String ListaDeEgresos(Model model){
        model.addAttribute("egresos",egresoServicio.ListarGastos());
        int anio = LocalDate.now().getYear();
        int mes = LocalDate.now().getMonthValue();
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDateTime inicio = primerDia.atStartOfDay();
        LocalDateTime fin = primerDia.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        model.addAttribute("datos",egresoServicio.DatosEgresos(inicio,fin));
        return "viewEgresos/index";
    }
    @GetMapping("eliminar/{id}")
    public String Eliminar(@PathVariable Long id, RedirectAttributes flash){
        try{
            egresoServicio.deleteGasto(id);
            flash.addFlashAttribute("success","eliminado Correctamente");
            return "redirect:/egresos/listar";
        }catch (Exception e){
            flash.addFlashAttribute("error", "Error al eliminar");
            return "redirect:/egresos/listar";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Egresos egreso = egresoServicio.ObtenerEgreso(id);
        model.addAttribute("egreso", egreso);
        return "viewEgresos/editarEgreso";
    }

    @PostMapping("/editar/{id}")
    public String editarEgreso(@PathVariable Long id,
                               @ModelAttribute("egreso") Egresos egreso,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "viewEgresos/editarEgreso";
        }

        try {
            // 1. Buscamos el egreso existente para no perder datos que no están en el form (como la fecha original)
            Egresos egresoExistente = egresoServicio.ObtenerEgreso(id);

            if (egresoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "El egreso no existe.");
                return "redirect:/egresos/listar";
            }

            // 2. Actualizamos solo los campos permitidos
            egresoExistente.setTipoEgreso(egreso.getTipoEgreso());
            egresoExistente.setMonto(egreso.getMonto());
            egresoExistente.setDescripcion(egreso.getDescripcion());

            // La fechaRegistro y el Usuario suelen mantenerse iguales a menos que decidas lo contrario

            // 3. Guardamos los cambios
            egresoServicio.UpdateEgreso(egresoExistente);

            redirectAttributes.addFlashAttribute("success", "Egreso actualizado correctamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
        }

        return "redirect:/egresos/listar";
    }
}

