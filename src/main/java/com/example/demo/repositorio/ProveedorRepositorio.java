package com.example.demo.repositorio;

import com.example.demo.entidad.Proveedores;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepositorio extends JpaRepository<Proveedores,Long> {
}
