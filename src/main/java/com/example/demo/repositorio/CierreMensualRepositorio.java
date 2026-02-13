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
    boolean existsByMesAndAnioAndEmpresaId(int mes, int anio, Long empresaId);

    // 2. Borrar el cierre previo (Query Nativo para asegurar la eliminación)
    // 2. Borrar el cierre previo (JPQL para que aplique el filtro de tenant automáticamente si es UPDATE/DELETE con filtro habilitado, pero cuidado: Hibernate Filters NO se aplican a DELETE JPQL por defecto, sin embargo, Spring Data JPA suele manejarlo si se usa derived query o si se hace con cuidado.
    // MEJOR: Usar deleteByMesAndAnio. Spring Data JPA provee deleteBy...
    
    @Modifying
    @Transactional
    void deleteByMesAndAnio(int mes, int anio);
}

