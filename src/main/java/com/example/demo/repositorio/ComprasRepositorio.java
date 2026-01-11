package com.example.demo.repositorio;

import com.example.demo.entidad.Compras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ComprasRepositorio extends JpaRepository<Compras,Long> {

    @Query(value = "SELECT nextval(referencia_compra_seq)",nativeQuery = true)
    Long obtenerNumeroSigReferencia();
}
