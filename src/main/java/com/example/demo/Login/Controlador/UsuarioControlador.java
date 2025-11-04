package com.example.demo.Login.Controlador;


import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("registro")
public class UsuarioControlador {


    @Autowired
    private ServicioUsuario usuarioservico;

    @ModelAttribute
    public UsuarioDTO usuarioDTO(){
        return new UsuarioDTO();
    }

    @GetMapping
    public String MostrarFormulario(){
        return "Login/registro";
    }

    @PostMapping("/nuevo")
    public String RegistrarUsuario(@ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO){
        try{
            usuarioservico.saveUser(usuarioDTO);
            return "redirect:/registro?exito";
        } catch (Exception e) {
            System.out.println("error en el registro"+ e.getMessage());
            return "redirect:/registro?error";
        }

    }


}
