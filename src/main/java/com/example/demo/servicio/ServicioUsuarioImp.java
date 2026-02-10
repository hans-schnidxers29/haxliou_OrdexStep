package com.example.demo.servicio;

import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.Usuario;
import com.example.demo.repositorio.RepositorioUsuario;
import com.example.demo.entidad.Rol;
import com.example.demo.entidad.Empresa;
import com.example.demo.repositorio.UsuarioEmpresaRepositorio;
import jakarta.transaction.Transactional;
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
public class ServicioUsuarioImp implements ServicioUsuario {

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioEmpresaRepositorio usuarioEmpresaRepositorio;

    @Autowired
    private SecurityService securityService;



    @Override
    public Usuario saveUser(Usuario usuario) {
        // Asignamos el rol por defecto (ROLE_ADMIN o ROLE_USER según prefieras)
        usuario.setRoles(Arrays.asList(new Rol("ROLE_USER")));

        // Encriptamos la contraseña que viene del objeto
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Guardamos el objeto completo con los campos de empresa
        return repositorioUsuario.save(usuario);
    }

    @Override
    public void updateUser(Usuario usuario, Long id) {
        try {
            Usuario userExistente = repositorioUsuario.findById(id)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Actualizamos datos personales
            userExistente.setNombre(usuario.getNombre());
            userExistente.setApellido(usuario.getApellido());
            userExistente.setEmail(usuario.getEmail());
            userExistente.setTelefono(usuario.getTelefono());
            userExistente.setDireccionCasa(usuario.getDireccionCasa());

            // Si la contraseña no es nula ni vacía, la actualizamos encriptada
            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                userExistente.setPassword(passwordEncoder.encode(usuario.getPassword()));
            }

            repositorioUsuario.save(userExistente);
        } catch (Exception e) {
            System.out.println("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @Override
    public Usuario findByEmail(String email) {
        return repositorioUsuario.findByEmail(email);
    }

    @Override
    @Transactional
    public void ActualizarRol(Long idUser, Rol rol) {
        Usuario usuario = repositorioUsuario.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Validación verifica si el String "ROLE_ADMIN" está presente en la lista de roles
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(r -> r.getNombre().equalsIgnoreCase("ROLE_ADMIN"));

        if (esAdmin) {
            throw new RuntimeException("Protección de Seguridad: No se permite degradar o cambiar cuentas de administrador.");
        }
        // Actualizamos la lista con el nuevo rol
        usuario.setRoles(Arrays.asList(rol));
    }

    @Override
    @Transactional
    public void actualizarContrasena(Long idUser, String password) {
        Usuario user = repositorioUsuario.findById(idUser)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String encodedPassword = passwordEncoder.encode(password);
        user.setPassword(encodedPassword);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repositorioUsuario.findByEmail(username);
        if (usuario == null) {
            throw new UsernameNotFoundException("Usuario o password inválidos");
        }
        // Spring Security seguirá usando el email y password para el login
        return new User(usuario.getEmail(), usuario.getPassword(), mapearAutoridadesRoles(usuario.getRoles()));
    }

    @Override
    public Usuario finbyyId(Long id) {
        return repositorioUsuario.findById(id).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @Override
    public List<Usuario> ListarUSer() {
        return usuarioEmpresaRepositorio.listaUsuarios(securityService.obtenerEmpresaId());
    }

    @Override
    public void deleteUser(Long id) {
        repositorioUsuario.deleteById(id);
    }

    private Collection<? extends GrantedAuthority> mapearAutoridadesRoles(Collection<Rol> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getNombre())).collect(Collectors.toList());
    }
    @Override
    public Usuario saveUserRolADmin(Usuario usuario) {
        // Asignamos el rol por defecto (ROLE_ADMIN o ROLE_USER según prefieras)
        usuario.setRoles(Arrays.asList(new Rol("ROLE_ADMIN")));

        // Encriptamos la contraseña que viene del objeto
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Guardamos el objeto completo con los campos de empresa
        return repositorioUsuario.save(usuario);
    }
    @Override
    public Long ObtenreIdEmpresa(Long id) {
        return repositorioUsuario.buscarIdEmpresaPorUsuarioId(id);
    }

    @Override
    public Empresa ObtenerEmpresa(Long id) {
        return repositorioUsuario.ObtenerEmpresaPorUsuarioId(id)
                .orElseThrow(()->new RuntimeException("Empresa no encontrada para el usuario: " + id));
    }


}