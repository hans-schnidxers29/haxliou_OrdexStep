package com.example.demo.servicio;

import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.Proveedores;
import com.example.demo.repositorio.ProveedorRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProveedorServicoImp implements ProveedorServicio{

    @Autowired
    private ProveedorRepositorio repositorio;

    @Autowired
    private SecurityService securityService;

    @Override
    public void save(Proveedores proveedore) {
        proveedore.setEmpresa(securityService.ObtenerEmpresa());
        repositorio.save(proveedore);
    }

    @Override
    public List<Proveedores> listarproveedores() {
        return repositorio.findByEmpresaId(securityService.obtenerEmpresaId()).stream()
                .filter(Proveedores::isEstado).toList();
    }

    @Override
    public Proveedores proveedorById(Long id) {
        return repositorio.findById(id).orElseThrow(
               ()-> new RuntimeException("Proveedor no encontrado"));
    }

    @Transactional
    @Override
    public void deleteProveedorById(Long id) {
        Proveedores proveedores = proveedorById(id);
        if(!proveedores.isEstado()){
            throw new IllegalStateException("El proveedor no se puede eliminar");
        }
        proveedores.setEstado(false);
        save(proveedores);
    }

    @Override
    public void updateProveedor(Long id, Proveedores proveedores) {
        Proveedores newprovedor = proveedorById(id);
        newprovedor.setId(id);
        newprovedor.setNombre(proveedores.getNombre());
        newprovedor.setDireccion(proveedores.getDireccion());
        newprovedor.setTelefono(proveedores.getTelefono());
        newprovedor.setEmail(proveedores.getEmail());
        newprovedor.setRazonSocial(proveedores.getRazonSocial());
        newprovedor.setNumeroDocumento(proveedores.getNumeroDocumento());
        newprovedor.setTipoDocumento(proveedores.getTipoDocumento());
        save(newprovedor);
    }

    @Override
    public boolean VerificarProveedor(Long id) {
        return repositorio.existsById(id);
    }

    }
