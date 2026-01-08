package com.example.demo.repositorio;

import com.example.demo.entidad.Egresos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EgresoRepositorio extends JpaRepository <Egresos, Long>{
}
