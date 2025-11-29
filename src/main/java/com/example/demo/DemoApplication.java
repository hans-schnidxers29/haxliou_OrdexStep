package com.example.demo;

import com.example.demo.Login.Repositorio.RepositorioUsuario;
import com.example.demo.Login.Repositorio.RolRepositorio;
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
public class DemoApplication  {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
