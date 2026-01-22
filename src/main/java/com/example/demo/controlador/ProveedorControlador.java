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
        return "ViewProveedor/listarProveedor";
    }

    @GetMapping("/crear")
    public String mostrarFormulario(Model model) {
        model.addAttribute("proveedor", new Proveedores());
        return "ViewProveedor/crearProveedor";
    }

    @PostMapping("/crear")
    public String guardarProveedor(
            @ModelAttribute("proveedor") Proveedores proveedor,
            RedirectAttributes redirect) {

        proveedorServicio.save(proveedor);

        redirect.addFlashAttribute("success",
                "Proveedor creado correctamente");

        return "redirect:/proveedores/listar";
    }



    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable Long id,
                                    RedirectAttributes redirect) {
        proveedorServicio.deleteProveedorById(id);
        redirect.addFlashAttribute("success",
                "Proveedor eliminado correctamente");
        return "redirect:/proveedores/listar";
    }

    @GetMapping("/editar/{id}")
    public String editarProveedorForm(@PathVariable Long id, Model model){
        model.addAttribute("proveedor",proveedorServicio.proveedorById(id));
        return "ViewProveedor/editarProveedor";
    }

    @PostMapping("/editar")
    public String actualizarProveedor(
            @ModelAttribute("proveedor") Proveedores proveedor,
            RedirectAttributes redirect) {
        proveedorServicio.save(proveedor);
        redirect.addFlashAttribute("success",
                "Proveedor actualizado correctamente");
        return "redirect:/proveedores/listar";
    }


}
