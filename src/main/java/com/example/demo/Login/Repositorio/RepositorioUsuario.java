package com.example.demo.Login.Repositorio;

import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
     Usuario findByEmail(String email);

     @Query("SELECT ue.empresa FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId")
     Optional<Empresa> buscarIdEmpresaPorUsuarioId(@Param("usuarioId") Long usuarioId);
}
