package com.example.demo.Login.Controlador;

import com.example.demo.Login.Rol;
import com.example.demo.entidad.Empresa;
import com.example.demo.Login.Servicio.ServicioEmpresa;
import com.example.demo.Login.Usuario;
import com.example.demo.Login.Servicio.RolServicio;
import com.example.demo.Login.Servicio.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("registro")
public class UsuarioControlador {

    @Autowired
    private ServicioUsuario usuarioservico;
    @Autowired
    private ServicioEmpresa servicioEmpresa;
    @Autowired
    private RolServicio rolServicio;

    // Ya no necesitamos el método @ModelAttribute de UsuarioDTO.
    // Usaremos directamente la entidad Usuario para todo.

    @GetMapping
    public String MostrarFormulario(Model model) {
        // Pasamos un objeto vacío de Usuario para el formulario de registro de empresa/persona
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("empresa", new Empresa());
        return "Login/registro";
    }

    @Transactional // Si falla la empresa, no se crea el usuario (Rollback)
    @PostMapping("/nuevo")
    public String RegistrarUsuario(
            @ModelAttribute("usuario") Usuario usuario,
            @ModelAttribute("empresa") Empresa empresa, // Recibe los campos 'name' del HTML
            RedirectAttributes flash) {
        try {
            // 1. Guardar el usuario (Esto genera su ID)
            // Asegúrate de que usuarioservico.saveUser(usuario) devuelva el usuario guardado
            Usuario usuarioGuardado = usuarioservico.saveUser(usuario);

            // 2. Validar si se enviaron datos de empresa (NIT es buen indicador)
            if (empresa.getNit() != null && !empresa.getNit().isBlank()) {

                // 3. Vincular la empresa con el usuario creado (ManyToOne)
                empresa.setPropietario(usuarioGuardado);

                // 4. Guardar la empresa (Necesitas el servicio de empresa inyectado)
                servicioEmpresa.saveEmpresa(empresa);
            }

            flash.addFlashAttribute("success", "Registro exitoso. Ya puede iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            System.out.println("Error en el registro: " + e.getMessage());
            flash.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
            // Importante: No uses redirect si quieres mostrar errores de validación,
            // pero para un catch de excepción general está bien.
            return "redirect:/registro";
        }
    }

    @GetMapping("/usuario")
    public String MostrarFormularioRegistro(Model model) {
        model.addAttribute("roles", rolServicio.listarRoles());
        model.addAttribute("usuario", new Usuario());
        return "viewUsuarios/RegistroUsuario";
    }

    @PostMapping("/usuario/nuevo")
    public String RegistraNuevoUsuario(@ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            int MaximosUsuarios = usuarioservico.ListarUSer().size();
            if (MaximosUsuarios >= 3) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten 3 usuarios");
                return "redirect:/registro/usuario";
            }
            // Usamos saveUser para que procese todos los campos nuevos
            usuarioservico.saveUser(usuario);
            redirectAttributes.addFlashAttribute("success", "Usuario creado exitosamente");
            return "redirect:/perfil";
        } catch (Exception e) {
            System.err.println("Error al guardar Usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar: " + e.getMessage());
            return "redirect:/registro/usuario";
        }
    }

    @GetMapping("/{id}")
    public String DeleteUsuario(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            usuarioservico.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Usuario eliminado exitosamente");
            return "redirect:/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar: " + e.getMessage());
            return "redirect:/perfil";
        }
    }

    @GetMapping("/editar/usuario/{id}")
    public String MostrarFormedit(@PathVariable Long id, Model model) {
        model.addAttribute("roles", rolServicio.listarRoles());
        model.addAttribute("usuario", usuarioservico.finbyyId(id));
        return "viewUsuarios/EditarUsuario";
    }

    @PostMapping("/editar/{id}")
    public String EditarUsuario(@PathVariable Long id, @ModelAttribute("usuario") Usuario usuario, RedirectAttributes redirectAttributes) {
        try {
            // El servicio ahora actualiza NIT, Razón Social, Direcciones, etc.
            usuarioservico.updateUser(usuario, id);
            redirectAttributes.addFlashAttribute("success", "Datos actualizados correctamente");
            return "redirect:/perfil";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/registro/editar/usuario/" + id;
        }
    }
    @PostMapping("/editar/contraseña/{id}")
    public String CambiarContrasena(@PathVariable Long id,
                                    @RequestParam("password") String nuevaPassword,
                                    RedirectAttributes flash) {
        try {
            // Validar que la contraseña no llegue vacía
            if (nuevaPassword == null || nuevaPassword.trim().isEmpty()) {
                flash.addFlashAttribute("error", "La contraseña no puede estar vacía");
                return "redirect:/registro/editar/usuario/" + id;
            }

            // Llamar al servicio pasando el ID y la NUEVA contraseña
            usuarioservico.actualizarContrasena(id, nuevaPassword);

            flash.addFlashAttribute("success", "Contraseña actualizada con éxito");
            return "redirect:/perfil";
        } catch (DataAccessException e) {
            flash.addFlashAttribute("error", "Error en la base de datos: " + e.getMessage());
            return "redirect:/perfil";
        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error inesperado: " + e.getMessage());
            return "redirect:/perfil";
        }
    }

    @GetMapping("/actualizar/rol/{id}")
    public String ActualizarRol(@PathVariable Long id, RedirectAttributes flash){
        try{
            Rol role = new Rol("ROLE_ADMIN");
            usuarioservico.ActualizarRol(id,role);
            flash.addFlashAttribute("success","Rol actualizado con exito");
            return "redirect:/perfil";
        }catch (Exception e){
            flash.addFlashAttribute("error","Error al actualizar rol"+ e.getMessage());
            return "redirect:/perfil";
        }
    }
}