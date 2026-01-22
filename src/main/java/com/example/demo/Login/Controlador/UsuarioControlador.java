package com.example.demo.Login.Controlador;

import com.example.demo.Login.Rol;
import com.example.demo.entidad.Empresa;
import com.example.demo.Login.Servicio.ServicioEmpresa;
import com.example.demo.Login.Usuario;
import com.example.demo.Login.Servicio.RolServicio;
import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.entidad.UsuarioEmpresa;
import com.example.demo.entidad.Enum.RolEmpresa;
import com.example.demo.repositorio.UsuarioEmpresaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("registro")
public class UsuarioControlador {

    @Autowired
    private ServicioUsuario usuarioservico;
    @Autowired
    private ServicioEmpresa servicioEmpresa;
    @Autowired
    private RolServicio rolServicio;

    @Autowired
    private UsuarioEmpresaRepositorio usuarioEmpresaRepositorio;

    // Ya no necesitamos el método @ModelAttribute de UsuarioDTO.
    // Usaremos directamente la entidad Usuario para todo.

    @GetMapping
    public String MostrarFormulario(Model model) {
        // Pasamos un objeto vacío de Usuario para el formulario de registro de empresa/persona
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("empresa", new Empresa());
        return "Login/registro";
    }

    @Transactional
    @PostMapping("/nuevo")
    public String RegistrarUsuario(
            @ModelAttribute("usuario") Usuario usuario,
            @ModelAttribute("empresa") Empresa empresa,
            RedirectAttributes flash) {
        try {
            Usuario usuarioGuardado = usuarioservico.saveUserRolADmin(usuario);

            if (empresa.getNit() != null && !empresa.getNit().isBlank()) {
                Empresa empresaGuardada = servicioEmpresa.saveEmpresa(empresa);

                UsuarioEmpresa ue = new UsuarioEmpresa();
                ue.setUsuario(usuarioGuardado);
                ue.setEmpresa(empresaGuardada);
                ue.setRol(RolEmpresa.PROPIETARIO);

                usuarioEmpresaRepositorio.save(ue);
            }

            flash.addFlashAttribute("success", "Registro exitoso. Ya puede iniciar sesión.");
            return "redirect:/login";

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al registrar: " + e.getMessage());
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
    @Transactional
    public String RegistraNuevoUsuario(@ModelAttribute("usuario") Usuario usuario,
                                       @AuthenticationPrincipal UserDetails userDetails,
                                       RedirectAttributes redirectAttributes) {
        try {
            int MaximosUsuarios = usuarioservico.ListarUSer().size();
            if (MaximosUsuarios >= 3) {
                redirectAttributes.addFlashAttribute("error", "Solo se permiten 3 usuarios");
                return "redirect:/registro/usuario";
            }

            // 1) Usuario que está registrando (logueado)
            Usuario creador = usuarioservico.findByEmail(userDetails.getUsername());
            if (creador == null) {
                redirectAttributes.addFlashAttribute("error", "No se pudo identificar el usuario actual.");
                return "redirect:/registro/usuario";
            }

            // 2) Obtener la empresa del creador desde usuario_empresa
            List<UsuarioEmpresa> relaciones = usuarioEmpresaRepositorio.findByUsuarioId(creador.getId());
            if (relaciones == null || relaciones.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tu usuario no tiene empresa asignada. No puedes crear usuarios.");
                return "redirect:/registro/usuario";
            }

            // Preferimos la relación PROPIETARIO si existe; si no, tomamos la primera
            UsuarioEmpresa relacionPrincipal = relaciones.stream()
                    .sorted(Comparator.comparing((UsuarioEmpresa ue) -> ue.getRol() == RolEmpresa.PROPIETARIO ? 0 : 1))
                    .findFirst()
                    .orElseThrow();

            Long empresaId = relacionPrincipal.getEmpresa().getId();
            Empresa empresa = servicioEmpresa.DatosEmpresa(empresaId);

            // 3) Crear el usuario nuevo (esto asigna ROLE_USER por tu ServicioUsuarioImp.saveUser)
            Usuario usuarioGuardado = usuarioservico.saveUser(usuario);

            // 4) Crear la relación usuario_empresa para el usuario nuevo dentro de LA MISMA empresa
            UsuarioEmpresa ueNuevo = new UsuarioEmpresa();
            ueNuevo.setUsuario(usuarioGuardado);
            ueNuevo.setEmpresa(empresa);
            ueNuevo.setRol(RolEmpresa.EMPLEADO); // o PROPIETARIO si lo decides por UI

            usuarioEmpresaRepositorio.save(ueNuevo);

            redirectAttributes.addFlashAttribute("success", "Usuario creado y asignado a tu empresa exitosamente");
            return "redirect:/perfil";

        } catch (Exception e) {
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