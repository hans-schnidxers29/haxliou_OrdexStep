package com.example.demo.servicio;

import com.example.demo.entidad.Empresa;


public interface ServicioEmpresa {

    Empresa saveEmpresa(Empresa empresa);
    Empresa DatosEmpresa(Long id);
    boolean existePorNit(String nit);

}
