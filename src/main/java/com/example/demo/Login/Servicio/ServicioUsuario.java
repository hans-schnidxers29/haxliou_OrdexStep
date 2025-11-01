package com.example.demo.Login.Servicio;

import com.example.demo.Login.Usuario;
import com.example.demo.Login.UsuarioDTO;

public interface ServicioUsuario {

    Usuario saveUser(UsuarioDTO usuarioDTO);
    Usuario finAllById(Long id);

}
