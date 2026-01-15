package com.example.demo.repositorio;

import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.entidad.Pedidos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface PedidoRepositorio extends JpaRepository<Pedidos,Long> {

    long count();

    @Query("SELECT COUNT(p) FROM Pedidos p WHERE p.estado = :estado")
    long contarPorEstado(@Param("estado") EstadoPedido estado);

    // Query para contar pedidos PENDIENTES
    @Query("SELECT COUNT(p) FROM Pedidos p WHERE p.estado = :estado")
    Long countByEstadoPendiente(@Param("estado") EstadoPedido estado);

    // Query para contar pedidos ENTREGADOS
    @Query("SELECT COUNT(p) FROM Pedidos p WHERE p.estado = :estado")
    Long countByEstadoEntregado(@Param("estado") EstadoPedido estado);

    // Query para contar pedidos CANCELADOS
    @Query("SELECT COUNT(p) FROM Pedidos p WHERE p.estado = :estado")
    Long countByEstadoCancelado(@Param("estado") EstadoPedido estado);

    @Query("SELECT COALESCE(SUM(p.total - (p.total / (1 + p.impuesto/100))), 0) " +
            "FROM Pedidos p " +
            "WHERE p.fechaPedido BETWEEN :inicio AND :fin " +
            "AND p.estado = 'COMPLETADO'")
    BigDecimal sumaImpuestosPedidos(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedidos p " +
            "WHERE p.fechaPedido BETWEEN :inicio AND :fin " +
            "AND p.estado = :estado")
    BigDecimal sumaTotalPedidosPorEstado(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin, @Param("estado") EstadoPedido estado);

    @Query("SELECT COUNT(p) FROM Pedidos p " +
            "WHERE p.fechaPedido BETWEEN :inicio AND :fin")
    Long cantidadPedidosPorRango(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

}
