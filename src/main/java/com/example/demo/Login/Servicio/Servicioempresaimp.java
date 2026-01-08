package com.example.demo.Login.Servicio;

import com.example.demo.entidad.Empresa;
import com.example.demo.Login.Repositorio.EmpresaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Servicioempresaimp implements ServicioEmpresa{

    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    @Override
    public void saveEmpresa(Empresa empresa) {
        empresaRepositorio.save(empresa);
    }

    @Override
    public Empresa DatosEmpresa(Long id) {
        Empresa empresa = empresaRepositorio.findById(id).orElseThrow(
                () -> new RuntimeException("Empresa no encontrada"));
        return empresa;
    }
}
