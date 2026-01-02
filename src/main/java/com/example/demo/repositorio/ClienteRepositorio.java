package com.example.demo.repositorio;


import com.example.demo.entidad.Cliente;
import groovy.transform.RecordBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepositorio  extends JpaRepository<Cliente,Long> {

    boolean existsByNumeroIdentificacion(String numeroIdentificacion);
    Cliente findByNumeroIdentificacion(String numeroIdentificacion);

    @Query(value = "SELECT c.nombre , \n" +
            "  COUNT(p.id_cliente) as cantidad_pedidos\n" +
            "  from pedidos as p , cliente as c\n" +
            "  where p.estado = 'ENTREGADO' and p.id_cliente = c.id\n" +
            "GROUP by c.nombre ORDER by cantidad_pedidos DESC", nativeQuery = true)
    List<Object []>CantidadPorPedidos();
}