package com.example.demo.controlador;
import com.example.demo.entidad.Categoria;
import com.example.demo.entidad.Productos;
import com.example.demo.servicio.CategoriaService;
import com.example.demo.servicio.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


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
    public String guardarproducto(@ModelAttribute("productos") Productos producto,RedirectAttributes redirectAttributes){
        try {
            Productos productoGuardado = service.save(producto);
            redirectAttributes.addFlashAttribute("success", "Producto guardado correctamente");
            System.out.println("Producto guardado con ID: " + productoGuardado.getNombre());
            return "redirect:/listarproductos";
        } catch (Exception e) {
            System.err.println("Error al guardar producto: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Error al guardar producto: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/crearproducto/nuevo";
        }
    }

    @GetMapping("/producto/actualizar/{id}")
    public String MostrarafromEditar(@PathVariable Long id, Model model){
        model.addAttribute("producto",service.productoById(id));
        model.addAttribute("categoria",serviceCate.Listarcategoria());
        return "viewProductos/actualizarProductos";
    }

    @PostMapping("/productos/{id}")
    public String ActualizarProducto(@PathVariable Long id,
                                     @ModelAttribute("producto") Productos producto,
                                     RedirectAttributes redirectAttributes) {
        try {
            // Buscar producto actual
            Productos productoActual = service.productoById(id);

            if (productoActual == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/listarproductos";
            }

            // Buscar categoría desde la BD
            Categoria categoria = serviceCate.Buscarbyid(producto.getCategoria().getId());

            if (categoria == null) {
                redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
                return "redirect:/producto/actualizar/" + id;
            }

            // Actualizar campos
            productoActual.setNombre(producto.getNombre());
            productoActual.setPrecio(producto.getPrecio());
            productoActual.setCantidad(producto.getCantidad());
            productoActual.setDescripcion(producto.getDescripcion());
            productoActual.setCategoria(categoria);  // ✅ Categoría gestionada

            // Guardar
            service.updateProductro(id, productoActual);

            redirectAttributes.addFlashAttribute("success", "Producto actualizado correctamente");
            return "redirect:/listarproductos";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/producto/actualizar/" + id;
        }
    }

    @GetMapping("/productos/delete/{id}")
    public String EliminarProdutos(@PathVariable Long id, RedirectAttributes redirectAttributes){
        redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        service.deleteProductoById(id);
        return "redirect:/listarproductos";
    }




}
