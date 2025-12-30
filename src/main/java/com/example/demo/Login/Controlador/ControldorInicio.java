package com.example.demo.Login.Controlador;


import com.example.demo.Login.Servicio.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControldorInicio {


    @Autowired
    private ServicioUsuario servicioUsuario;

    @GetMapping("/login")
    public String iniciarSesion() {
        return "Login/login";
    }

    @GetMapping({"/","/Home"})
    public String verPaginaDeInicio(Model modelo) {
        modelo.addAttribute("usuarios", servicioUsuario.ListarUSer());
        return "Home/Home";
    }

}
