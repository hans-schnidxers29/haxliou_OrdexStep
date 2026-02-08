
package com.example.demo.Seguridad;

import com.example.demo.entidad.Empresa;
import com.example.demo.entidad.Usuario;
import com.example.demo.servicio.ServicioUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    @Autowired
    private ServicioUsuario servicioUser;
    /**
     * Extrae el ID de la empresa del usuario autenticado.
     * @return Long con el ID de la empresa
     * @throws RuntimeException si no hay usuario logueado o el tipo es incorrecto
     */
    public Long obtenerEmpresaId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetails user) {
            Usuario usuario = servicioUser.findByEmail(user.getUsername());
            return servicioUser.ObtenreIdEmpresa(usuario.getId());
        }

        throw new IllegalStateException("No se pudo encontrar una empresa vinculada a la sesión actual.");
    }

    public String obtenerEmailUsuario() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public Empresa ObtenerEmpresa(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof UserDetails user) {
            Usuario usuario = servicioUser.findByEmail(user.getUsername());
            return servicioUser.ObtenerEmpresa(usuario.getId());
        }

        throw new IllegalStateException("No se pudo encontrar una empresa vinculada a la sesión actual.");
    }

    public boolean estaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth.getPrincipal() instanceof String && auth.getPrincipal().equals("anonymousUser"));
    }
}
