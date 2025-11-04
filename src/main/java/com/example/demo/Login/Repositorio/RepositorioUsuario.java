package com.example.demo.Login.Repositorio;

import com.example.demo.Login.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
    public Usuario findByEmail(String email);
}
