package com.example.demo.repositorio;

import com.example.demo.entidad.CierreMensual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CierreMensualRepositorio extends JpaRepository<CierreMensual, Long>{


    // 1. Verificar si existe (Spring lo genera solo por el nombre)
    boolean existsByMesAndAnio(int mes, int anio);

    // 2. Borrar el cierre previo (Query Nativo para asegurar la eliminación)
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM cierre_mensual WHERE mes = :mes AND anio = :anio", nativeQuery = true)
    void eliminarCierreExistente(@Param("mes") int mes, @Param("anio") int anio);
}

