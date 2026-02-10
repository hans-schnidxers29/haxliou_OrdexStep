package com.example.demo.repositorio;

import com.example.demo.entidad.Usuario;
import com.example.demo.entidad.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioEmpresaRepositorio extends JpaRepository<UsuarioEmpresa, Long> {
    List<UsuarioEmpresa> findByUsuarioId(Long usuarioId);
    List<UsuarioEmpresa> findByEmpresaId(Long empresaId);

    @Query("select ue.usuario from UsuarioEmpresa ue where ue.empresa.id = :empresaId")
    List<Usuario>listaUsuarios(@Param("empresaId")Long empresaId);
}
