package com.example.demo.Login.Controlador;


import com.example.demo.Login.UsuarioDTO;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("registro")
public class UsuarioControlador {

    @ModelAttribute
    public UsuarioDTO usuarioDTO(){
        return new UsuarioDTO();
    }

    @GetMapping
    public String MostrarFormulario(){
        return "Login/registro";
    }







}
