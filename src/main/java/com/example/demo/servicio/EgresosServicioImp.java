package com.example.demo.servicio;

import com.example.demo.entidad.Egresos;
import com.example.demo.repositorio.EgresoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EgresosServicioImp implements EgresoServicio{

    @Autowired
    private EgresoRepositorio repositorio;


    @Override
    public void CrearGasto(Egresos egresos) {
        repositorio.save(egresos);
    }

    @Override
    public List<Egresos> ListarGastos() {
        return repositorio.findAll().stream().toList();
    }
}
