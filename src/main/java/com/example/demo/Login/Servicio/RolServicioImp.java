package com.example.demo.Login.Servicio;

import com.example.demo.Login.Repositorio.RolRepositorio;
import com.example.demo.Login.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RolServicioImp  implements  RolServicio{

    @Autowired
    private RolRepositorio rolRepositorio;


    @Override
    public List<Rol> listarRoles() {
        return rolRepositorio.findAll();
    }
}
