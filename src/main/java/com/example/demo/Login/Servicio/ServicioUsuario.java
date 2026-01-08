package com.example.demo.Login.Servicio;

import com.example.demo.Login.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface ServicioUsuario extends UserDetailsService {
    Usuario saveUser(Usuario usuario);
    Usuario finbyyId(Long id);
    List<Usuario> ListarUSer();
    void deleteUser(Long id);
    void updateUser(Usuario usuario, Long id);
    Usuario findByEmail(String email);

}
