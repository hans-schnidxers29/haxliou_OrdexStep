package com.example.demo.repositorio;

import com.example.demo.entidad.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioEmpresaRepositorio extends JpaRepository<UsuarioEmpresa, Long> {
    List<UsuarioEmpresa> findByUsuarioId(Long usuarioId);
    List<UsuarioEmpresa> findByEmpresaId(Long empresaId);
}
