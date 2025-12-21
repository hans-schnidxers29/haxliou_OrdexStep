package com.example.demo.repositorio;

import com.example.demo.entidad.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DetalleReposirotio extends JpaRepository<DetallePedido,Long> {

    @Query("SELECT SUM(producto.id) FROM DetallePedido")
    Long SumaProductos();


}
