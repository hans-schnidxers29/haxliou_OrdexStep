package com.example.demo.controlador;

import com.example.demo.entidad.Categoria;
import com.example.demo.entidad.Enum.TipoVenta;
import com.example.demo.entidad.Productos;
import com.example.demo.entidad.Proveedores;
import com.example.demo.repositorio.TributoRepositorio;
import com.example.demo.repositorio.UnidadesMedidasRepositorio;
import com.example.demo.servicio.CategoriaService;
import com.example.demo.servicio.ProductoServicio;
import com.example.demo.servicio.ProveedorServicio;
import com.example.demo.servicio.UnidadMedidaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
public class ProductoControlador {

    @Autowired
    private ProductoServicio service;

    @Autowired
    private CategoriaService serviceCate;

    @Autowired
    private ProveedorServicio proveedorServicio;

    @Autowired
    private UnidadMedidaServicio unidadRepo;

    @Autowired
    private TributoRepositorio tributoRepositorio;


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
        model.addAttribute("tiposVenta", unidadRepo.Listademedidas());
        model.addAttribute("proveedores", proveedorServicio.listarproveedores());
        model.addAttribute("tributo",tributoRepositorio.findAll());

        return "viewProductos/crearProductos";
    }

    @PostMapping("/crearproducto")
    public String guardarproducto(@ModelAttribute("productos") Productos producto, RedirectAttributes redirectAttributes){
        try {
            // Spring MVC mapeará automáticamente el campo 'impuesto' desde el formulario
            Productos productoGuardado = service.save(producto);
            redirectAttributes.addFlashAttribute("success", "Producto guardado correctamente");
            return "redirect:/listarproductos";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al guardar producto: " + e.getMessage());
            return "redirect:/crearproducto/nuevo";
        }
    }

    @GetMapping("/producto/actualizar/{id}")
    public String MostrarafromEditar(@PathVariable Long id, Model model){
        model.addAttribute("producto",service.productoById(id));
        model.addAttribute("categoria",serviceCate.Listarcategoria());
        model.addAttribute("tiposVenta", unidadRepo.Listademedidas());
        model.addAttribute("proveedores", proveedorServicio.listarproveedores());
        model.addAttribute("tributo",tributoRepositorio.findAll());
        return "viewProductos/actualizarProductos";
    }

    @PostMapping("/productos/{id}")
    public String ActualizarProducto(@PathVariable Long id,
                                     @ModelAttribute("producto") Productos producto,
                                     RedirectAttributes redirectAttributes) {
        try {
            Productos productoActual = service.productoById(id);

            if (productoActual == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/listarproductos";
            }

            // 1. Validar Categoría
            Categoria categoria = serviceCate.Buscarbyid(producto.getCategoria().getId());
            if (categoria == null) {
                redirectAttributes.addFlashAttribute("error", "Categoría no encontrada");
                return "redirect:/producto/actualizar/" + id;
            }

            // 2. Validar Proveedor
            if (producto.getProveedor() != null && producto.getProveedor().getId() != null) {
                Proveedores proveedorBD = proveedorServicio.proveedorById(producto.getProveedor().getId());
                productoActual.setProveedor(proveedorBD);
            } else {
                productoActual.setProveedor(null);
            }

            if(producto.getPrecioCompra()== null && productoActual.getPrecioCompra()!=null){
                producto.setPrecioCompra(productoActual.getPrecioCompra());
            }
            if(producto.getPrecioPorMayor() == null ){
                productoActual.setPrecioPorMayor(BigDecimal.ZERO);
            }

            // --- ACTUALIZACIÓN DE CAMPOS ---
            productoActual.setNombre(producto.getNombre());
            productoActual.setPrecio(producto.getPrecio()); // Precio de Venta

            // NUEVO: Seteamos el precio de compra desde el formulario
            productoActual.setPrecioCompra(producto.getPrecioCompra());

            productoActual.setCantidad(producto.getCantidad());
            productoActual.setDescripcion(producto.getDescripcion());
            productoActual.setCategoria(categoria);
            productoActual.setTipoVenta(producto.getTipoVenta());
            productoActual.setCantidadMinima(producto.getCantidadMinima());
            productoActual.setIncremento(producto.getIncremento());
            productoActual.setStockMinimo(producto.getStockMinimo());
            productoActual.setImpuesto(producto.getImpuesto());

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
        service.deleteProductoById(id);
        redirectAttributes.addFlashAttribute("success", "Producto eliminado correctamente");
        return "redirect:/listarproductos";
    }

    @GetMapping("/producto/stock/{id}")
    public String MostrarStock(@PathVariable Long id, Model model){
        model.addAttribute("producto",service.productoById(id));
        return "viewProductos/Agregar-Stock";
    }

    @PostMapping("producto/stock-agregar/{id}")
    public String AgregarStock(@PathVariable Long id, @RequestParam BigDecimal cantidad, RedirectAttributes redirectAttributes){
        try{
            BigDecimal impuesto = BigDecimal.ZERO;
            BigDecimal NewPrecioCompra = BigDecimal.ZERO;
            service.AgregarStock(id,cantidad,impuesto,NewPrecioCompra);
            redirectAttributes.addFlashAttribute("success","Stock agregado correctamente");
            return "redirect:/listarproductos";
        }catch (Exception e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error","Error al agregar stock: " + e.getMessage());
            return "redirect:/listarproductos";
        }
    }
}