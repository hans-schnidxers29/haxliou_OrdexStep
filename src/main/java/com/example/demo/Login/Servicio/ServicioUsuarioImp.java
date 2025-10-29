package com.example.demo.Login.Servicio;


import com.example.demo.Login.Repositorio.RepositorioUsuario;
import com.example.demo.Login.Rol;
import com.example.demo.Login.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ServicioUsuarioImp implements ServicioUsuario{

    @Autowired
    private RepositorioUsuario repositorioUsuario;
    @Autowired
    private ServicioUsuario servicioUsuario;


    @Override
    public Usuario saveUser(Usuario usuarioDTO) {
        Usuario usuario = new Usuario(usuarioDTO.getNombre(),usuarioDTO.getApellido(),usuarioDTO.getEmail(),
                                    usuarioDTO.getPassword(), Arrays.asList(new Rol("ROLE_ADMIN")));
        return servicioUsuario.saveUser(usuario);
    }
}
