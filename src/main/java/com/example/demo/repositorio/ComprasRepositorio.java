package com.example.demo.repositorio;

import com.example.demo.entidad.Compras;
import com.example.demo.entidad.Enum.TipoVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComprasRepositorio extends JpaRepository<Compras,Long> {


    List<Compras>findByEmpresaId(Long empresa_id);

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(referencia, 6) AS INTEGER)), 0) + 1 " +
            "FROM compras WHERE empresa_id = :empresaId", nativeQuery = true)
    Long obtenerNumeroSigReferencia(@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Compras c WHERE c.fechaCompra BETWEEN :inicio AND :fin")
    BigDecimal sumTotalCompras(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    @Query(value = "SELECT SUM(d.cantidad) " +
            "FROM detalle_compra d " +
            "JOIN compras c ON c.id = d.compras_id " +
            "JOIN productos p ON p.id = d.productos_id " +
            "JOIN unidad_medida um ON um.id = p.unidad_medida_id " +
            "WHERE c.estado = :estado " +
            "AND um.code = :tipoVenta " + // Filtramos por el código (String)
            "AND c.fecha_compra BETWEEN :inicio AND :fin",
            nativeQuery = true)
    BigDecimal sumarTotalEntrantePorTipoYRango(
            @Param("tipoVenta") String tipoVenta, // Aquí pasas 'UND', 'KG', etc.
            @Param("estado") String estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}
