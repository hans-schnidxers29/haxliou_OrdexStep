package com.example.demo.repositorio;

import com.example.demo.entidad.Usuario;
import com.example.demo.entidad.Empresa;
import com.example.demo.entidad.UsuarioEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioUsuario extends JpaRepository<Usuario, Long> {
     Usuario findByEmail(String email);

     @Query("SELECT ue.empresa.id FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId")
     Long buscarIdEmpresaPorUsuarioId(@Param("usuarioId") Long usuarioId);


     @Query("SELECT ue.empresa FROM UsuarioEmpresa ue WHERE ue.usuario.id = :usuarioId")
     Optional<Empresa> ObtenerEmpresaPorUsuarioId(@Param("usuarioId") Long usuarioId);

     @Query("SELECT ue.usuario FROM UsuarioEmpresa ue where ue.empresa = :empresa_id")
     List<Usuario>ListaUsuariosEmpresas(@Param("empresa_id") Long  Empresa_id);
}
