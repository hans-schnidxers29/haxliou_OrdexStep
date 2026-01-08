package com.example.demo.Login.Servicio;

import com.example.demo.entidad.Empresa;


public interface ServicioEmpresa {

    void saveEmpresa(Empresa empresa);

    Empresa DatosEmpresa(Long id);

}
