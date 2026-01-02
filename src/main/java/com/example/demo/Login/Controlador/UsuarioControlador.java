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
    public String RegistrarUsuario(@ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO, RedirectAttributes flash) {
        try {
            usuarioservico.saveUser(usuarioDTO);
            flash.addFlashAttribute("success", "Usuario registrado exitosamente");
            return "redirect:/registro";  // ✅ Sin ?success=true
        } catch (Exception e) {
            System.out.println("error en el registro" + e.getMessage());
            flash.addFlashAttribute("error", "Error al registrar el usuario: " + e.getMessage());
            return "redirect:/registro";  // ✅ Sin ?error=true
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
    public String RegistraNuevoUsuario(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes){
        try {
            usuarioservico.saveUserDto(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario registrado exitosamente");
            return "redirect:/perfil";  // ✅ Sin ?success
        } catch (Exception e) {
            System.err.println("Error al guardar Usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar usuario: " + e.getMessage());
            return "redirect:/registro/usuario";  // ✅ Sin ?error=true
        }
    }

    @GetMapping("/{id}")
    public String DeleteUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes){
        try {
            usuarioservico.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente");
            return "redirect:/perfil";
        } catch (Exception e) {
            System.err.println("Error al eliminar Usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al eliminar usuario: " + e.getMessage());
            return "redirect:/perfil";
        }
    }

    @GetMapping("/editar/usuario/{id}")
    public String MostrarFormedit(@PathVariable Long id, Model model){
        model.addAttribute("roles", rolServicio.listarRoles());
        model.addAttribute("usuario", usuarioservico.finbyyId(id));
        return "viewUsuarios/EditarUsuario";
    }

    @PostMapping("/editar/{id}")
    public String EditarUsuario(@PathVariable Long id, @ModelAttribute("usuarios") Usuario usuarios, RedirectAttributes redirectAttributes){
        try {
            usuarioservico.updateUser(usuarios, id);
            redirectAttributes.addFlashAttribute("success", "Usuario actualizado exitosamente");
            return "redirect:/perfil";  // ✅ Sin ?success
        } catch (Exception e) {
            System.err.println("Error al actualizar Usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al actualizar usuario: " + e.getMessage());
            return "redirect:/registro/editar/usuario/" + id;  // ✅ Volver al formulario de edición
        }
    }
    @GetMapping("/test")
    public String test() {
        return "test";
    }
}