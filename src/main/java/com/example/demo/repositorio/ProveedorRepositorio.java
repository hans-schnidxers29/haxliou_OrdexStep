package com.example.demo.repositorio;

import com.example.demo.entidad.Proveedores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProveedorRepositorio extends JpaRepository<Proveedores,Long> {
    // ✅ Simplificado: usar findAll() con filtro automático
    // List<Proveedores> findByEmpresaId(Long empresa_id);
}
