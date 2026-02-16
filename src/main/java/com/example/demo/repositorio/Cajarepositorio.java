package com.example.demo.repositorio;

import com.example.demo.entidad.Usuario;
import com.example.demo.entidad.Caja;
import com.example.demo.entidad.Enum.EstadoDeCaja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface Cajarepositorio extends JpaRepository<Caja, Long> {

      @Query("""
          SELECT (COUNT(c) > 0)
          FROM Caja c
          WHERE c.usuario = :usuario AND c.Estado = :estado AND c.empresa.id = :empresaId
          """)
      boolean existsByUsuarioAndEstadoAndEmpresaId(@Param("usuario") Usuario usuario,
                                                   @Param("estado") EstadoDeCaja estado,
                                                   @Param("empresaId") Long empresaId);

      @Query("""
                SELECT c
                FROM Caja c
                WHERE c.usuario = :usuario AND c.Estado = :estado AND c.empresa.id = :empresaId
             """)
      Optional<Caja> findByUsuarioAndEstadoAndEmpresaId(@Param("usuario") Usuario usuario,
                                                        @Param("estado") EstadoDeCaja estado,
                                                        @Param("empresaId") Long empresaId);


}


