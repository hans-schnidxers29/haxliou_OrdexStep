package com.example.demo.repositorio;

import com.example.demo.entidad.UnidadMedidas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnidadesMedidasRepositorio extends JpaRepository<UnidadMedidas,Integer> {
}
