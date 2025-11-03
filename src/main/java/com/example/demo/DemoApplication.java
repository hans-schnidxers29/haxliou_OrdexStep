package com.example.demo;

import com.example.demo.Login.Repositorio.RepositorioUsuario;
import com.example.demo.Login.Rol;
import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Categoria;
import com.example.demo.repositorio.CategoriaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}


    @Autowired
    private CategoriaRepositorio categoriaRepositorio;

    @Autowired
    private RepositorioUsuario usuer;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Categoria c1 = new Categoria("ELECTRODOMESTICOS","elementos caseros electronicos");
        categoriaRepositorio.save(c1);

        Categoria c2 = new Categoria("TECNOLOGICOS","elementos tecnologicos");
        categoriaRepositorio.save(c2);

        Categoria c3 = new Categoria("HOGAR","elementos caseros NO electronicos");
        categoriaRepositorio.save(c3);

        Categoria c4= new Categoria("COMIDA","Productos comestibles");
        categoriaRepositorio.save(c4);

        Categoria c5 = new Categoria("ASEO","productos de aseo");
        categoriaRepositorio.save(c5);

        Categoria c6= new Categoria("INMUEBLES","Productos de inmuebles decorativos");
        categoriaRepositorio.save(c6);

        // Seed default admin user only if not exists, and ensure password is BCrypt-encoded
        String email = "hans@gmail.com";
        if (usuer.findByEmail(email) == null) {
            String encoded = passwordEncoder.encode("hans");
            Usuario usuario = new Usuario("hans","patiño", email, encoded, Arrays.asList(new Rol("ROLE_ADMIN")));
            usuer.save(usuario);
        }
    }
}
