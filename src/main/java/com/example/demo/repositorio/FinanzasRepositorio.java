package com.example.demo.repositorio;

import com.example.demo.entidad.AbonosCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinanzasRepositorio extends JpaRepository<AbonosCompra,Long> {
}
