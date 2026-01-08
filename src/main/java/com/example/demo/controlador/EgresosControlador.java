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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
            return "redirect:/egresos/crear";

        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al crear el egreso: " + e.getMessage());
            return "redirect:/egresos/listar";
        }
    }

    @GetMapping("/listar")
    public String ListaDeEgresos(Model model){
        model.addAttribute("egresos",egresoServicio.ListarGastos());
        return "viewEgresos/index";
    }
}

