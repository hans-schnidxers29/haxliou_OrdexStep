package com.example.demo.Login.Servicio;

import com.example.demo.Login.Usuario;
import com.example.demo.Login.UsuarioDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface ServicioUsuario extends UserDetailsService {
    Usuario saveUser(UsuarioDTO usuarioDTO);
    Usuario finbyyId(Long id);
    List<Usuario> ListarUSer();
    Usuario saveUserDto(Usuario usuario);
    void deleteUser(Long id);
    void updateUser(Usuario usuario, Long id);

}
