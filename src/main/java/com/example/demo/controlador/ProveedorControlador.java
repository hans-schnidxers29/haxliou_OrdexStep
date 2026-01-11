package com.example.demo.controlador;
import com.example.demo.entidad.Proveedores;
import com.example.demo.servicio.ProveedorServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/proveedores")
public class ProveedorControlador {

    @Autowired
    private ProveedorServicio proveedorServicio;



    @GetMapping("/listar")
    public String Listar(Model model){
        model.addAttribute("proveedores",proveedorServicio.listarproveedores());
        return "viewProveedor/listarProveedor";
    }


    @GetMapping("/crear")
    public String Mostrarformulario(Model model){
        Proveedores p = new Proveedores();
        model.addAttribute("proveedor",p);
        return "viewProveedor/crearProveedor";
    }

    @PostMapping("crear/nuevo")
    public String CrearProveedor(@ModelAttribute("proveedor") Proveedores proveedor,
                                 RedirectAttributes redirectAttributes) {
        try{
            proveedorServicio.save(proveedor);
            redirectAttributes.addFlashAttribute("success", "Proveedor guardado correctamente");
            return "redirect:/proveedores/crear";
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al guardar el proveedor: " + e.getMessage());
            return "redirect:/proveedores/listar";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor( @PathVariable Long id, RedirectAttributes redirectAttributes){
        try{
            proveedorServicio.VerificarProveedor(id);
            proveedorServicio.deleteProveedorById(id);
            redirectAttributes.addFlashAttribute("success", "Proveedor eliminado correctamente");
            return "redirect:/proveedores/listar";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el proveedor: " + e.getMessage());
            return "redirect:/proveedores/listar";
        }
    }

    @GetMapping("/editar/{id}")
    public String editarProveedorForm(@PathVariable Long id, Model model){
        model.addAttribute("proveedor",proveedorServicio.proveedorById(id));
        return "viewProveedor/editarProveedor";
    }

//    @PostMapping("/editar/proveedor/{id}")
//    public String ActualizarProveedor(@PathVariable Long id,@Valid @ModelAttribute("proveedor") Proveedores proveedor,
//                                      BindingResult result, Model model, RedirectAttributes redirectAttributes){
//
//    }

}
