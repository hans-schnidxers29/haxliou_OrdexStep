package com.example.demo.servicio;

import com.example.demo.entidad.Proveedores;

import java.util.List;

public interface ProveedorServicio {

    void save(Proveedores proveedore);
    List<Proveedores> listarproveedores();
    Proveedores proveedorById(Long id);
    void deleteProveedorById(Long id);
    void updateProveedor(Long id,Proveedores proveedores);
    boolean VerificarProveedor(Long id );
}
