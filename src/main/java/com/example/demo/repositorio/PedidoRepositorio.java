package com.example.demo.repositorio;

import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.entidad.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedidos,Long> {

    List<Pedidos>findByEmpresaId(Long empresa_id);

    @Query("SELECT COUNT(p) FROM Pedidos p WHERE p.estado = :estado AND p.empresa.id = :empresa_id")
    long contarPorEstado(@Param("estado") EstadoPedido estado, @Param("empresa_id") Long empresa_id);


    @Query("SELECT COALESCE(SUM(p.total - (p.total / (1 + p.impuesto/100))), 0) " +
            "FROM Pedidos p " +
            "WHERE p.fechaPedido BETWEEN :inicio AND :fin " +
            "AND p.estado = 'COMPLETADO' AND p.empresa.id = :empresaId")
    BigDecimal sumaImpuestosPedidos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedidos p " +
            "WHERE p.fechaPedido BETWEEN :inicio AND :fin " +
            "AND p.estado = :estado AND p.empresa.id = :empresaId")
    BigDecimal sumaTotalPedidosPorEstado(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin, @Param("estado") EstadoPedido estado, @Param("empresaId") Long empresaId);


    @Query("SELECT COUNT(p) FROM Pedidos p " +
            "WHERE p.fechaPedido BETWEEN :inicio AND :fin " +
            "AND p.estado = :estado AND p.empresa.id = :empresaId")
    Long cantidadPedidosPorRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("estado") EstadoPedido estado, @Param("empresaId") Long empresaId);

}
