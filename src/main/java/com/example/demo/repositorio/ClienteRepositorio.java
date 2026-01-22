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

    @Query(value = "SELECT \n" +
            "    nombre, \n" +
            "    SUM(total_movimiento) as total_ventas\n" +
            "FROM (\n" +
            "    -- Parte de Pedidos\n" +
            "    SELECT c.nombre, p.total as total_movimiento\n" +
            "    FROM pedidos p\n" +
            "    JOIN cliente c ON p.id_cliente = c.id\n" +
            "    WHERE p.estado = 'ENTREGADO'\n" +
            "    AND c.numero_identificacion != '222222222222' -- Excluir Consumidor Final\n" +
            "\n" +
            "    UNION ALL\n" +
            "\n" +
            "    -- Parte de Ventas Directas\n" +
            "    SELECT c.nombre, v.total as total_movimiento\n" +
            "    FROM venta v\n" +
            "    JOIN cliente c ON v.cliente_id = c.id\n" +
            "    WHERE c.numero_identificacion != '222222222222' -- Excluir Consumidor Final\n" +
            ") as consolidado_ventas\n" +
            "GROUP BY nombre\n" +
            "ORDER BY total_ventas DESC\n" +
            "LIMIT 5;", nativeQuery = true) // Agregamos LIMIT 5 para el Top 5
    List<Object []> CantidadPorPedidos();

    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.fechaRegistro BETWEEN :inicio AND :fin")
    Integer contarNuevosClientesPorRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );

}