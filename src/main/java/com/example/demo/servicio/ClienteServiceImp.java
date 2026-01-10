package com.example.demo.servicio;


import com.example.demo.entidad.Cliente;
import com.example.demo.repositorio.ClienteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImp implements ClienteService {

    @Autowired
    private ClienteRepositorio repositorio;


    @Override
    public List<Cliente> listarcliente() {
        return repositorio.findAll();
    }

    @Override
    public Cliente save(Cliente cliente) {
        return repositorio.save(cliente);
    }

    @Override
    public Cliente clientdById(Long id) {
        return repositorio.findById(id).orElse(null);
    }

    @Override
    public void deleteclienteById(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    public void update(Cliente cliente) {
        repositorio.save(cliente);
    }

    @Override
    public List<Long> ListaCLientePedidos() {
        List<Object[]> resultado = repositorio.CantidadPorPedidos();
        return resultado.stream().map(objeto -> (Long) objeto[1]).toList();
    }

    @Override
    public List<String> NombreListPedidos() {
        List<Object[]>Resultado= repositorio.CantidadPorPedidos();
        return Resultado.stream().map(objeto -> (String) objeto[0]).toList();
    }

    @Override
    public boolean VerifcarCliente(String numeroIdentificacion) {
        return repositorio.existsByNumeroIdentificacion(numeroIdentificacion);
    }

    @Override
    public List<Map<String, Object>> clienteSimple() {
        List<Map<String, Object>> clientesSimplificados = repositorio.findAll().stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("nombre", c.getNombre());
            map.put("apellido", c.getApellido());
            map.put("numeroIdentificacion", c.getNumeroIdentificacion());
            map.put("email", c.getEmail());
            map.put("telefono", c.getTelefono());
            return map;
        }).collect(Collectors.toList());
        return  clientesSimplificados;
    }
}
