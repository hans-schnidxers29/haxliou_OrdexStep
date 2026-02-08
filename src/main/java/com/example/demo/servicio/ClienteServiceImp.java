package com.example.demo.servicio;


import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.Cliente;
import com.example.demo.entidad.Empresa;
import com.example.demo.repositorio.ClienteRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImp implements ClienteService {

    @Autowired
    private ClienteRepositorio repositorio;

    @Autowired
    private SecurityService securityService;


    @Override
    public List<Cliente> listarcliente() {
        Long EmpresaId = securityService.obtenerEmpresaId();
        return repositorio.findByEmpresaId(EmpresaId);
    }

    @Override
    public Cliente save(Cliente cliente) {
        Empresa empresa =  securityService.ObtenerEmpresa();
        cliente.setEmpresa(empresa);
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
    public boolean VerifcarCliente(String numeroIdentificacion) {
        return repositorio.existsByNumeroIdentificacionAndEmpresaId(numeroIdentificacion,securityService.obtenerEmpresaId());
    }

    @Override
    public List<Map<String, Object>> clienteSimple() {
        return listarcliente().stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("nombre", c.getNombre());
            map.put("apellido", c.getApellido());
            map.put("numeroIdentificacion", c.getNumeroIdentificacion());
            map.put("email", c.getEmail());
            map.put("telefono", c.getTelefono());
            return map;
        }).collect(Collectors.toList());
    }
    @Override
    public Map<String, Object> CantidadPedidosPorPersonas() {
        // 1. Obtenemos la lista de objetos del repositorio
        List<Object[]> resultados = repositorio.CantidadPorPedidos(securityService.obtenerEmpresaId());

        // 2. Creamos las listas para separar los datos
        List<String> nombres = new ArrayList<>();
        List<Long> cantidades = new ArrayList<>();

        // 3. Iteramos y extraemos la información por posición
        for (Object[] fila : resultados) {
            nombres.add((String) fila[0]);               // Posición 0: nombre
            cantidades.add(((Number) fila[1]).longValue()); // Posición 1: cantidad_pedidos
        }

        // 4. Retornamos el mapa con las listas separadas
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("nombres", nombres);
        respuesta.put("cantidades", cantidades);

        return respuesta;
    }
}
