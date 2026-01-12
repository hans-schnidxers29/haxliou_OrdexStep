package com.example.demo.repositorio;

import com.example.demo.entidad.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Cajarepositorio extends JpaRepository<Caja,Long> {
}
