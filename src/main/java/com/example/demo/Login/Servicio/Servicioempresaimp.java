package com.example.demo.Login.Servicio;

import com.example.demo.Login.Empresa;
import com.example.demo.Login.Repositorio.EmpresaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Servicioempresaimp implements ServicioEmpresa{

    @Autowired
    private EmpresaRepositorio empresaRepositorio;

    @Override
    public Empresa saveEmpresa(Empresa empresa) {
        return empresaRepositorio.save(empresa);
    }
}
