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
    boolean VerifcarCliente(String numeroIdentificacion);
    List<Map<String, Object>>clienteSimple();
    Map<String,Object>CantidadPedidosPorPersonas();
    List<Cliente>ClienteParaJax(String term);
}
