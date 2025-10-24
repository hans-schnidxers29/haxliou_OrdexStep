package com.example.demo.repositorio;


import com.example.demo.entidad.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepositorio  extends JpaRepository<Cliente,Long> {
}
