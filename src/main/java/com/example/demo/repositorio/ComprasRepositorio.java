package com.example.demo.repositorio;

import com.example.demo.entidad.Compras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComprasRepositorio extends JpaRepository<Compras,Long> {
}
