package com.example.demo.repositorio;

import com.example.demo.entidad.Tributo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TributoRepositorio extends JpaRepository<Tributo,Integer> {
}
