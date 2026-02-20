package com.example.demo.repositorio;

import com.example.demo.entidad.ComprasCreditos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface ComprasCreditoRepositorio extends JpaRepository<ComprasCreditos,Long> {
    ComprasCreditos findCompraById(Long id);

    Object findAllBySaldoPendienteGreaterThan(BigDecimal saldoPendienteIsGreaterThan);
}
