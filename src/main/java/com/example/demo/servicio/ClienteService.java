package com.example.demo.servicio;

import com.example.demo.entidad.Cliente;

import java.util.List;
import java.util.Map;

public interface ClienteService {
    List<Cliente> listarcliente();
    Cliente save(Cliente cliente);
    Cliente clientdById(Long id);
    void deleteclienteById(Long id);
    void update(Cliente cliente);
    List<Long>ListaCLientePedidos();
    List<String>NombreListPedidos();
    boolean VerifcarCliente(String numeroIdentificacion);
    List<Map<String, Object>> clienteSimple();
}
