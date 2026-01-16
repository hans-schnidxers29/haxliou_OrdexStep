package com.example.demo.repositorio;


import com.example.demo.entidad.Cliente;
import groovy.transform.RecordBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClienteRepositorio  extends JpaRepository<Cliente,Long> {

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
    Cliente findByNumeroIdentificacion(String numeroIdentificacion);

    @Query(value = "SELECT c.nombre, " +
            "COUNT(p.id) as cantidad_pedidos, " +
            "SUM(p.total) as total_ventas " + // Sumamos el monto de los pedidos
            "FROM pedidos p " +
            "JOIN cliente c ON p.id_cliente = c.id " +
            "WHERE p.estado = 'ENTREGADO' " +
            "GROUP BY c.id, c.nombre " + // Agrupar por ID es más seguro si hay nombres duplicados
            "ORDER BY total_ventas DESC", nativeQuery = true)
    List<Object []>CantidadPorPedidos();

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.fechaRegistro BETWEEN :inicio AND :fin")
    Integer contarNuevosClientesPorRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}