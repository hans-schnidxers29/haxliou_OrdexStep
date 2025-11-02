package com.example.demo.Login.Servicio;

import com.example.demo.Login.Usuario;
import com.example.demo.Login.UsuarioDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public interface ServicioUsuario extends UserDetailsService {

    Usuario saveUser(UsuarioDTO usuarioDTO);
    Usuario finAllById(Long id);
    List<Usuario> ListarUSer();
    Usuario findByEmial(String email);
}
