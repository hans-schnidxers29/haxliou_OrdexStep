package com.example.demo.controlador;


import com.example.demo.entidad.Cliente;
import com.example.demo.servicio.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class ClienteControlador {

    @Autowired
    private ClienteService service;

    @GetMapping("/home")
    public String home(){
        return "Home/Home";
    }


    @GetMapping("/listarclientes")
    public String listarclientes(Model model){
        model.addAttribute("clientes",service.listarcliente());
        return "viewCliente/index";
    }


    @GetMapping("/crearcliente/nuevo")
    public String Mostrarfrom(Model model){
        Cliente c = new Cliente();
        model.addAttribute("cliente",c);
        return "viewCliente/crearCliente";
    }

    @PostMapping("/crear")
    public String guardarcliente(@ModelAttribute Cliente cliente, Model model){
        try {
            service.save(cliente);
            return "redirect:/listarclientes";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("Error", "El correo electrónico ya está en uso.");
            model.addAttribute("cliente", cliente); // para no perder los datos ingresados
            return "redirect:/crearcliente/nuevo"; // asegúrate que esta sea tu vista del formulario
        }
    }

    @GetMapping("/cliente/actualizar/{id}")
    public String actualizarclienteForm(@PathVariable Long id, Model model){
        model.addAttribute("clientes",service.clientdById(id));
        return "viewCliente/actualizarCliente";
    }

    @PostMapping("/clientes/{id}")
    public String actualizarcliente(@PathVariable Long id, @ModelAttribute("cliente") Cliente cliente,Model model){
        Cliente clienteNew = service.clientdById(id);
        clienteNew.setId(id);
        clienteNew.setNombre(cliente.getNombre());
        clienteNew.setApellido(cliente.getApellido());
        clienteNew.setEmail(cliente.getEmail());
        service.update(clienteNew);
        return "redirect:/listarclientes";
    }

    @GetMapping("cliente/delete/{id}")
    public String deletecliente(@PathVariable Long id){
        service.deleteclienteById(id);
        return "redirect:/listarclientes";
    }




}
