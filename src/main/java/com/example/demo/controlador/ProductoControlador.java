package com.example.demo.controlador;
import com.example.demo.entidad.Productos;
import com.example.demo.servicio.CategoriaService;
import com.example.demo.servicio.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public class ProductoControlador {

    @Autowired
    private ProductoServicio service;

    @Autowired
    private CategoriaService serviceCate;

    @GetMapping("/listarproductos")
    public String listarProductos(Model model){
        model.addAttribute("productos",service.listarProductos());
        return "viewProductos/index";
    }

    @GetMapping("/crearproducto/nuevo")
    public String Mostrarfrom(Model model){
        Productos p = new Productos();
        model.addAttribute("categoria",serviceCate.Listarcategoria());
        model.addAttribute("producto",p);
        return "viewProductos/crearProductos";
    }

    @PostMapping("/crearproducto")
    public String guardarproducto(@ModelAttribute("productos") Productos producto){
        try {
            Productos productoGuardado = service.save(producto);
            System.out.println("Producto guardado con ID: " + productoGuardado.getNombre());
            return "redirect:/listarproductos";
        } catch (Exception e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/crearproducto/nuevo?error=true";
        }
    }

    @GetMapping("/producto/actualizar/{id}")
    public String MostrarafromEditar(@PathVariable Long id, Model model){
        model.addAttribute("producto",service.productoById(id));
        return "viewProductos/actualizarProductos";
    }


    @PostMapping("productos/{id}")
    public String ActualizarProducto(@PathVariable Long id, @ModelAttribute("productos") Productos productos, Model model){
        Productos productoActual = service.productoById(id);
        productoActual.setId(id);
        productoActual.setNombre(productos.getNombre());
        productoActual.setPrecio(productos.getPrecio());
        productoActual.setCantidad(productos.getCantidad());
        productoActual.setCategoria(productos.getCategoria());
        productoActual.setDescripcion(productos.getDescripcion());
        service.updateProductro(productoActual);
        return "redirect:/listarproductos";
    }

    @GetMapping("/productos/delete/{id}")
    public String EliminarProdutos(@PathVariable Long id){
        service.deleteProductoById(id);
        return "redirect:/listarproductos";
    }




}
