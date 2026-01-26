package com.example.demo.servicio;

import com.example.demo.entidad.UnidadMedidas;
import com.example.demo.repositorio.UnidadesMedidasRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class UnidadMedidasServicioImpl implements UnidadMedidaServicio{

    @Autowired
    private UnidadesMedidasRepositorio unidadrepo;


    @Override
    public List<UnidadMedidas> Listademedidas() {
        return unidadrepo.findAll();
    }
}
