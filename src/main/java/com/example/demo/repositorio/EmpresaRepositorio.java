package com.example.demo.repositorio;

import com.example.demo.entidad.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpresaRepositorio extends JpaRepository<Empresa,Long> {
    boolean existsByNit(String nit);
}
