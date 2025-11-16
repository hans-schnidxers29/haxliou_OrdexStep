package com.example.demo.Login.Repositorio;

import com.example.demo.Login.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepositorio extends JpaRepository<Rol,Long> {
}
