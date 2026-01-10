package com.example.demo.servicio;

import com.example.demo.entidad.Compras;
import com.example.demo.repositorio.ComprasRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompraServicoImp implements CompraServicio{

    @Autowired
    private ComprasRepositorio repositorio;

    @Override
    public void saveCompra(Compras compras) {

    }

    @Override
    public List<Compras> listarCompra() {

        return List.of();
    }

    @Override
    public Compras compraById(Long id) {
        return null;
    }

    @Override
    public void deleteCompraById(Long id) {

    }

    @Override
    public void updateCompra(Long id, Compras compras) {

    }

    @Override
    public boolean verifcarCompra(Long id) {
        return false;
    }
}
