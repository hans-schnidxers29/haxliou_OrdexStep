package com.example.demo.repositorio;

import com.example.demo.entidad.Compras;
import com.example.demo.entidad.Enum.TipoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface ComprasRepositorio extends JpaRepository<Compras,Long> {

    @Query(value = "SELECT nextval('referencia_compra_seq')",nativeQuery = true)
    Long obtenerNumeroSigReferencia();

    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Compras c WHERE c.fechaCompra BETWEEN :inicio AND :fin")
    BigDecimal sumTotalCompras(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT SUM(d.cantidad) " +
            "FROM detalle_compra d " +
            "JOIN compras c ON c.id = d.compras_id " +
            "JOIN productos p ON p.id = d.productos_id " +
            "WHERE c.estado = :estado " + 
            "AND p.tipo_venta = :tipoVenta " +
            "AND c.fecha_compra BETWEEN :inicio AND :fin",
            nativeQuery = true)
    BigDecimal sumarTotalEntrantePorTipoYRango(
            @Param("tipoVenta") String tipoVenta, 
            @Param("estado") String estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}
