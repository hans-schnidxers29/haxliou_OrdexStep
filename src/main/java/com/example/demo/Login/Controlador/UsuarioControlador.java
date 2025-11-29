package com.example.demo.Login.Controlador;


import com.example.demo.Login.Servicio.RolServicio;
import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.Usuario;
import com.example.demo.Login.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("registro")
public class UsuarioControlador {


    @Autowired
    private ServicioUsuario usuarioservico;

    @Autowired
    private RolServicio rolServicio;

    @ModelAttribute
    public UsuarioDTO usuarioDTO(){
        return new UsuarioDTO();
    }

    @GetMapping
    public String MostrarFormulario(){
        return "Login/registro";
    }

    @PostMapping("/nuevo")
    public String RegistrarUsuario(@ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO) {
        try {
            usuarioservico.saveUser(usuarioDTO);
            return "redirect:/registro?exito";
        } catch (Exception e) {
            System.out.println("error en el registro" + e.getMessage());
            return "redirect:/registro?error";
        }

    }

    @GetMapping("/usuario")
    public String MostrarFromularioRegsitro(Model model){
        Usuario usuario = new Usuario();
        model.addAttribute("roles", rolServicio.listarRoles());
        model.addAttribute("usuario", usuario);
        return "viewUsuarios/RegistroUsuario";
    }

    @PostMapping("/usuario/nuevo")
    public String RegistraNuevoUsuario(@ModelAttribute ("usuario")Usuario usuario, RedirectAttributes redirectAttributes){
        try {
            usuarioservico.saveUserDto(usuario);
            return "redirect:/perfil?success";
        }catch (Exception e){
            System.err.println("Error al guardar Usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar Usuario: " + e.getMessage());
             return "redirect:/registro/usuario?error";
        }
    }

    @GetMapping("/{id}")
    public String DeleteUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes){
        try{
            usuarioservico.deleteUser(id);
            return "redirect:/perfil?success";
        }catch (Exception e){
            System.err.println("Error al guardar Usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar Usuario: " + e.getMessage());
            return "redirect:/registro/usuario?error";
        }
    }

    @GetMapping("/editar/usuario/{id}")
    public String MostrarFormedit(@PathVariable Long id,Model model){
        model.addAttribute("roles", rolServicio.listarRoles());
        model.addAttribute("usuario", usuarioservico.finbyyId(id));
        return "viewUsuarios/EditarUsuario";
    }

    @PostMapping("/editar/{id}")
    public String EditarUsuario(@PathVariable Long id, @ModelAttribute("usuarios") Usuario usuarios, RedirectAttributes redirectAttributes){
        try {
            usuarioservico.updateUser(usuarios,id);
            return "redirect:/perfil?success";
        }catch (Exception e){
            System.err.println("Error al guardar Usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Error al guardar Usuario: " + e.getMessage());
            return "redirect:/registro/usuario?error";
        }
    }


}
