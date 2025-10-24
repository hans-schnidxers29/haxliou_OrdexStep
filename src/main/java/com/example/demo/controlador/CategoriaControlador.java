package com.example.demo.controlador;


import com.example.demo.entidad.Categoria;
import com.example.demo.servicio.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CategoriaControlador {

    @Autowired
    private CategoriaService service;

    @GetMapping("/categoria/crear")
    public String Moatrarfrom(Model model){
        Categoria c = new Categoria();
        model.addAttribute("categorias",c);
        return "viewCategorias/crearCategoria";
    }

    @PostMapping("/categoria/nueva")
    public String Crearcategoria(@ModelAttribute("categoria") Categoria categoria){
        service.savecategoria(categoria);
        return "redirect:/crearproducto/nuevo";
    }

}
