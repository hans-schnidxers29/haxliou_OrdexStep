package com.example.demo.repositorio;

import com.example.demo.entidad.AbonosCompra;
import com.example.demo.entidad.Enum.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FinanzasRepositorio extends JpaRepository<AbonosCompra,Long> {

    @Query("select coalesce(sum(" +
            " a.monto),0) from Pagos a " +
            "join  a.abonosCompra cc " +
            "where cc.salioCaja = true and cc.metodoPago in (:metodosPago) " +
            "and cc.fechaAbono between :inicio and :fin and cc.cuentaPorPagar.compra.empresa.id = :empresaId and a.metodoPago = 'EFECTIVO' ")
    BigDecimal sumarAbonosCompraBySalioCajaEfectivo(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin,
                                                    @Param("empresaId") Long empresaId, @Param("metodosPago") List<MetodoPago> metodosPago);
}
