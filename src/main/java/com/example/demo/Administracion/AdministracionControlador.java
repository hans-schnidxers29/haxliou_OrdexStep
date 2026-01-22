package com.example.demo.Administracion;

import com.example.demo.Login.Repositorio.EmpresaRepositorio;
import com.example.demo.Login.Repositorio.RepositorioUsuario;
import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/")
public class AdministracionControlador {

    @Value("${api.admin.key}")
    private String adminApiKey;


    @Autowired
    private EmpresaRepositorio empresaRepository;

    @Autowired
    private RepositorioUsuario usuarioRepository;

    @GetMapping("/empresas")
    public ResponseEntity<?> listarEmpresas(@RequestHeader("X-API-KEY") String headerApiKey) {

        // 1. Validar si la cabecera existe y es correcta
        if (headerApiKey == null || !headerApiKey.equals(adminApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Acceso Denegado: API Key inválida");
        }

        // 2. Extraer los datos (Empresas y Usuarios)
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("empresas", empresaRepository.findAll());
        respuesta.put("usuarios", usuarioRepository.findAll());
        respuesta.put("total_empresas", empresaRepository.count());

        return ResponseEntity.ok(respuesta);
    }
}
