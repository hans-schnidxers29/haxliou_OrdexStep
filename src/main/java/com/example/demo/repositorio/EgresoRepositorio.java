package com.example.demo.repositorio;

import com.example.demo.entidad.Egresos;
import com.example.demo.entidad.Empresa;
import com.example.demo.entidad.Enum.MetodoPago;
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

    @Query(" SELECT COALESCE(SUM(e.monto), 0) FROM Egresos e WHERE e.fechaRegistro BETWEEN :inicio AND :fin AND e.empresa.id = :empresaId")
    BigDecimal sumarEgresosPorDia(@Param("inicio") LocalDateTime inicio,
                                  @Param("fin") LocalDateTime fin,
                                  @Param("empresaId") Long empresaId);

    @Query(" SELECT COALESCE(SUM(e.monto), 0) FROM Egresos e WHERE e.fechaRegistro BETWEEN :inicio AND :fin " +
            "AND e.tipoEgreso = :tipoegreso AND e.empresa.id = :empresaId")
    BigDecimal SumaEgresosPorTipo(@Param("inicio")LocalDateTime inicio,
                                  @Param("fin")LocalDateTime fin,
                                  @Param("tipoegreso") TipoEgreso egreso,
                                  @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(e.monto), 0) FROM Egresos e WHERE e.empresa.id = :empresaId  " +
            "and e.salioCaja = true and e.fechaRegistro between :inicio and :fin")
    BigDecimal GastosDeCaja(@Param("inicio")LocalDateTime inicio, @Param("fin")LocalDateTime fin, @Param("empresaId") Long empresaId);


    @Query("select coalesce(sum( p.monto),0) from Pagos p join p.egresos e " +
            "where  e.salioCaja = true and e.metodoPago in( :metodos) and p.metodoPago = 'EFECTIVO' " +
            "AND p.fechaPago between :inicio  and :fin and e.empresa.id = :empresaId")
    BigDecimal egresosSalioCaja(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                                @Param("metodos")List<MetodoPago> metodos, @Param("empresaId") Long empresaId);

}
