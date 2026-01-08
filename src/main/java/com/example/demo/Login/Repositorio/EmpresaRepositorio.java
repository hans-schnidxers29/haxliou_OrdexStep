package com.example.demo.Login.Repositorio;

import com.example.demo.entidad.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepositorio extends JpaRepository<Empresa,Long> {
}
