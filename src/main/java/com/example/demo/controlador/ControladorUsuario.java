package com.example.demo.controlador;

import com.example.demo.servicio.ServicioUsuario;
import com.example.demo.entidad.Usuario;
import org.springframework.beans.factory.annotation .Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControladorUsuario {

    @Autowired
    private ServicioUsuario servicioUsuario;


    @GetMapping("/perfil")
    public String mostrarPerfil(Model modelo) {
        modelo.addAttribute("usuarios", servicioUsuario.ListarUSer());
        modelo.addAttribute("usuario", new Usuario());
        return "viewUsuarios/Perfil";
    }







































}
