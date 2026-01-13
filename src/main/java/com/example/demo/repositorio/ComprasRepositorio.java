package com.example.demo.repositorio;

import com.example.demo.entidad.Compras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface ComprasRepositorio extends JpaRepository<Compras,Long> {

    @Query(value = "SELECT nextval(referencia_compra_seq)",nativeQuery = true)
    Long obtenerNumeroSigReferencia();


    @Query(value = "select coalesce(sum(e.total),0) as total_Compras_hoy \n" +
            "from compras e\n" +
            "where fecha_Compra >= :inicio \n" +
            "and  fecha_Compra <= :fin",nativeQuery = true)
    BigDecimal sumaEgresosHoy(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
