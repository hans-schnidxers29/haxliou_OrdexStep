package com.example.demo.Login.Servicio;

import com.example.demo.Login.Empresa;


public interface ServicioEmpresa {

    void saveEmpresa(Empresa empresa);

    Empresa DatosEmpresa(Long id);

}
