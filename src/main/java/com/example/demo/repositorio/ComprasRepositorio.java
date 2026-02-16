package com.example.demo.repositorio;

import com.example.demo.entidad.Compras;
import com.example.demo.entidad.Enum.MetodoPago;
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

    @Query(value = "SELECT COALESCE(\n" +
            "    MAX(\n" +
            "        CAST(\n" +
            "            NULLIF(regexp_replace(SUBSTRING(numero_referencia, 6), '\\D', '', 'g'), '') \n" +
            "            AS INTEGER\n" +
            "        )\n" +
            "    ), 0\n" +
            ") + 1 \n" +
            "FROM compras \n" +
            "WHERE empresa_id = :empresaId", nativeQuery = true)
    Long obtenerNumeroSigReferencia(@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Compras c WHERE c.fechaCompra BETWEEN :inicio AND :fin AND c.empresa.id = :empresaId ")
    BigDecimal sumTotalCompras(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                               @Param("empresaId") Long empresaId);

    @Query(value = "SELECT SUM(d.cantidad) " +
            "FROM detalle_compra d " +
            "JOIN compras c ON c.id = d.compras_id " +
            "JOIN productos p ON p.id = d.productos_id " +
            "JOIN unidad_medida um ON um.id = p.unidad_medida_id " +
            "WHERE c.estado = :estado " +
            "AND um.code = :tipoVenta " + // Filtramos por el código (String)
            "AND c.fecha_compra BETWEEN :inicio AND :fin AND c.empresa_id = :empresaId AND p.empresa_id = c.empresa_id",
            nativeQuery = true)
    BigDecimal sumarTotalEntrantePorTipoYRango(
            @Param("tipoVenta") String tipoVenta, // Aquí pasas 'UND', 'KG', etc.
            @Param("estado") String estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("empresaId") Long empresaId
    );
}
