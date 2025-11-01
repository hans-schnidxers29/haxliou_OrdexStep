package com.example.demo.Login.Servicio;


import com.example.demo.Login.Repositorio.RepositorioUsuario;
import com.example.demo.Login.Rol;
import com.example.demo.Login.Usuario;
import com.example.demo.Login.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class ServicioUsuarioImp implements ServicioUsuario{

    @Autowired
    private RepositorioUsuario repositorioUsuario;



    @Override
    public Usuario saveUser(UsuarioDTO usuarioDTO) {
        Usuario usuario = new Usuario(usuarioDTO.getNombre(),usuarioDTO.getApellido(),usuarioDTO.getEmail(),
                                    usuarioDTO.getPassword(), Arrays.asList(new Rol("ROLE_ADMIN")));
        return repositorioUsuario.save(usuario);
    }

    @Override
    public Usuario finAllById(Long id) {
        return repositorioUsuario.findById(id).orElseThrow();
    }
}
