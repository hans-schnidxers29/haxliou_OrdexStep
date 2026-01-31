package com.example.demo.servicio;

import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.Categoria;
import com.example.demo.entidad.Empresa;
import com.example.demo.repositorio.CategoriaRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoriaServiceimp implements CategoriaService{


    @Autowired
    private CategoriaRepositorio repositorio;

    @Autowired
    private SecurityService securityService;


    @Override
    public List<Categoria> Listarcategoria() {
        Long empresaId=securityService.obtenerEmpresaId();
        return repositorio.findByEmpresaId(empresaId);
    }

    @Override
    public Categoria savecategoria(Categoria categoria) {
        Empresa empresa = securityService.ObtenerEmpresa();
        categoria.setEmpresa(empresa);
        return repositorio.save(categoria);
    }

    @Override
    public Categoria Buscarbyid(Long id) {
        return repositorio.findById(id)
                .orElseThrow(()->new RuntimeException("categoria no encontrada"));
    }

    @Override
    public List<Map<String, Object>> Categorias() {
        Long Empresaid= securityService.obtenerEmpresaId();
        return repositorio.findByEmpresaId(Empresaid).stream().map(c ->{
            Map<String,Object> datos = new HashMap<>();
            datos.put("id",c.getId());
            datos.put("nombre",c.getNombrecategoria());
            return datos;
        }).collect(Collectors.toList());
    }
}
