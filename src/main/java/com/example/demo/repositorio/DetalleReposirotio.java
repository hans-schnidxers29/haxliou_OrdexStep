package com.example.demo.repositorio;

import com.example.demo.entidad.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DetalleReposirotio extends JpaRepository<DetallePedido,Long> {

    @Query("SELECT SUM(dp.producto.id) FROM DetallePedido dp WHERE dp.pedido.empresa.id = :empresaId")
    Long SumaProductos(@Param("empresaId") Long empresaId);


}
