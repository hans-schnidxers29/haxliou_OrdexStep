package com.example.demo.repositorio;

import com.example.demo.entidad.Egresos;
import com.example.demo.entidad.Enum.TipoEgreso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EgresoRepositorio extends JpaRepository <Egresos, Long>{

    List<Egresos> findByEmpresaId(Long empresa_id);

    @Query(" SELECT COALESCE(SUM(e.monto), 0) FROM Egresos e WHERE e.fechaRegistro BETWEEN :inicio AND :fin")
    BigDecimal sumarEgresosPorDia(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin);

    @Query(" SELECT COALESCE(SUM(e.monto), 0) FROM Egresos e WHERE e.fechaRegistro BETWEEN :inicio AND :fin " +
            "AND e.tipoEgreso = :tipoegreso")
    BigDecimal SumaEgresosPorTipo(@Param("inicio")LocalDateTime inicio,
                                  @Param("fin")LocalDateTime fin,
                                  @Param("tipoegreso") TipoEgreso egreso);

}
