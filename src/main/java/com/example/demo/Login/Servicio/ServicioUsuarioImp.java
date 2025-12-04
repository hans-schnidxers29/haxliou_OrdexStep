package com.example.demo.Login.Servicio;


import com.example.demo.Login.Repositorio.RepositorioUsuario;
import com.example.demo.Login.Rol;
import com.example.demo.Login.Usuario;
import com.example.demo.Login.UsuarioDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicioUsuarioImp implements ServicioUsuario{

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public Usuario saveUser(UsuarioDTO usuarioDTO) {
        String rolaAdmin = new String();
        rolaAdmin = "ROLE_ADMIN";

        Usuario usuario = new Usuario(usuarioDTO.getNombre(),usuarioDTO.getApellido(),usuarioDTO.getEmail(),
                passwordEncoder.encode(usuarioDTO.getPassword()),Arrays.asList(new Rol(rolaAdmin)));
        return repositorioUsuario.save(usuario);
    }

    @Override
    public Usuario finbyyId(Long id) {
        return repositorioUsuario.findById(id).orElseThrow(()-> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public List<Usuario> ListarUSer() {
        return repositorioUsuario.findAll();
    }

    @Override
    public Usuario saveUserDto(Usuario usuario) {
        Usuario user = new Usuario(usuario.getId(), usuario.getNombre(), usuario.getApellido(),
                usuario.getEmail(), passwordEncoder.encode(usuario.getPassword()),Arrays.asList(new Rol("ROLE_USER"))
        );
        return repositorioUsuario.save(user) ;
    }

    @Override
    public void deleteUser(Long id) {
        repositorioUsuario.findById(id).orElseThrow(()-> new RuntimeException("Usuario no encontrado"));
        repositorioUsuario.deleteById(id);
    }

    @Override
    public void updateUser(Usuario usuario, Long id) {
        try {
            Usuario user = repositorioUsuario.findById(id).orElseThrow(()-> new RuntimeException("usuario no encontrado"));
            user.setId(id);
            user.setNombre(usuario.getNombre());
            user.setApellido(usuario.getApellido());
            user.setEmail(usuario.getEmail());
            user.setRoles(usuario.getRoles());
            repositorioUsuario.save(user);
        }catch (Exception e){
            System.out.println("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repositorioUsuario.findByEmail(username);
        if(usuario == null) {
            throw new UsernameNotFoundException("Usuario o password inválidos");
        }
        return new User(usuario.getEmail(),usuario.getPassword(), mapearAutoridadesRoles(usuario.getRoles()));
    }

    private Collection<? extends GrantedAuthority> mapearAutoridadesRoles(Collection<Rol> roles){
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getNombre())).collect(Collectors.toList());
    }
}