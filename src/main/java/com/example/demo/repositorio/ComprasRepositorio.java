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


    // ✅ Simplificado: usar findAll() con filtro automático
    // List<Compras> findByEmpresaId(Long empresa_id);

    @Query(value = "SELECT COALESCE(\n" +
            "    MAX(\n" +
            "        CAST(\n" +
            "            NULLIF(regexp_replace(SUBSTRING(numero_referencia, 6), '\\D', '', 'g'), '') \n" +
            "            AS INTEGER\n" +
            "        )\n" +
            "    ), 0\n" +
            ") + 1 \n" +
            "FROM compras WHERE empresa_id = :empresaId", nativeQuery = true)
    Long obtenerNumeroSigReferencia(@Param("empresaId") Long empresaId);

    @Query("SELECT COALESCE(SUM(c.total), 0) FROM Compras c WHERE c.fechaCompra BETWEEN :inicio AND :fin ")
    BigDecimal sumTotalCompras(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    // JPQL Automáticamente aplica el filtro de tenant sobre 'c' (Compras) y 'p' (Productos)
    @Query("SELECT SUM(d.cantidad) " +
            "FROM DetalleCompra d " +
            "JOIN d.compra c " +
            "JOIN d.productos p " +
            "JOIN p.tipoVenta um " +
            "WHERE c.estado = :estado " +
            "AND um.Code = :tipoVenta " +
            "AND c.fechaCompra BETWEEN :inicio AND :fin")
    BigDecimal sumarTotalEntrantePorTipoYRango(
            @Param("tipoVenta") String tipoVenta,
            @Param("estado") com.example.demo.entidad.Enum.EstadoCompra estado,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin
    );
}
