package com.example.demo.servicio;

import com.example.demo.entidad.Empresa;
import com.example.demo.repositorio.EmpresaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Servicioempresaimp implements ServicioEmpresa{

    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    @Override
    public Empresa saveEmpresa(Empresa empresa) {
        empresaRepositorio.save(empresa);
        return empresa;
    }

    @Override
    public Empresa DatosEmpresa(Long id) {
        Empresa empresa = empresaRepositorio.findById(id).orElseThrow(
                () -> new RuntimeException("Empresa no encontrada"));
        return empresa;
    }
}
