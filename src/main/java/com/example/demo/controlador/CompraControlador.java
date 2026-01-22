package com.example.demo.controlador;

import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Compras;
import com.example.demo.entidad.DetalleCompra;
import com.example.demo.entidad.Enum.EstadoCompra;
import com.example.demo.entidad.Enum.MetodoPago;
import com.example.demo.entidad.Productos;
import com.example.demo.entidad.Proveedores;
import com.example.demo.servicio.CompraServicio;
import com.example.demo.servicio.ProductoServicio;
import com.example.demo.servicio.ProveedorServicio;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/compras")
public class CompraControlador {

    @Autowired
    private CompraServicio compraServicio;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Autowired
    private ProveedorServicio proveedorServicio;

    @Autowired
    private ProductoServicio productoServicio;

    @GetMapping("/listar")
    public String listar(Model model){

        int anio = LocalDate.now().getYear();
        int mes = LocalDate.now().getMonthValue();
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDateTime inicio = primerDia.atStartOfDay();
        LocalDateTime fin = primerDia.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);

        model.addAttribute("Compras", compraServicio.listarCompra());
        model.addAttribute("Datos",compraServicio.StokMensual(inicio,fin));

        return "viewCompras/listarCompras";
    }

    @GetMapping("/crear")
    public String Mostrarformulario(Model model){

        Compras compras = new Compras();
        model.addAttribute("compras",compras);
        model.addAttribute("proveedores",proveedorServicio.listarproveedores());
        model.addAttribute("productos",productoServicio.listarProductos());
        model.addAttribute("MetodoPago", MetodoPago.values());

        return "viewCompras/crearCompras";
    }


    @PostMapping("/crear")
    public String crearCompra(@Valid @ModelAttribute("compras") Compras compras,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model,
                              @AuthenticationPrincipal UserDetails userDetails) {

        // 1. Validación de anotaciones @NotNull, @Min, etc.
        if (result.hasErrors()) {
            // No redirigir, sino devolver la misma vista
            model.addAttribute("proveedores", proveedorServicio.listarproveedores());
            model.addAttribute("productos", productoServicio.listarProductos());
            return "viewCompras/crearCompras";
        }

        try {
            // 2. Asignar Usuario autenticado
            Usuario user = servicioUsuario.findByEmail(userDetails.getUsername());
            compras.setUsuario(user);

            // 3. Validar Proveedor
            Proveedores proveedor = proveedorServicio.proveedorById(compras.getProveedor().getId());
            if (proveedor == null) {
                // Aquí también devolvemos la misma vista en lugar de redirect
                model.addAttribute("error", "Proveedor no encontrado");
                model.addAttribute("proveedores", proveedorServicio.listarproveedores());
                model.addAttribute("productos", productoServicio.listarProductos());
                return "viewCompras/crearCompras";
            }
            compras.setProveedor(proveedor);

            // 4. Procesar Detalles de Compra
            if (compras.getDetalles() == null || compras.getDetalles().isEmpty()) {
                model.addAttribute("info", "La lista de productos no puede estar vacía");
                model.addAttribute("proveedores", proveedorServicio.listarproveedores());
                model.addAttribute("productos", productoServicio.listarProductos());
                return "viewCompras/crearCompras";
            }

            List<DetalleCompra> detallesValidos = new ArrayList<>();
            BigDecimal acumuladorSubtotales = BigDecimal.ZERO;

            for (DetalleCompra detalle : compras.getDetalles()) {
                if (detalle.getProductos() == null || detalle.getProductos().getId() == null ||
                        detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0 ||
                        detalle.getPrecioUnitario() == null) {
                    continue;
                }

                Productos productoCompleto = productoServicio.productoById(detalle.getProductos().getId());
                if (productoCompleto == null) continue;


                if (detalle.getProductos().getImpuesto() != null) {
                    productoCompleto.setImpuesto(detalle.getProductos().getImpuesto());
                }

                if(detalle.getProductos().getPrecioCompra() != null){
                    productoCompleto.setPrecioCompra(detalle.getProductos().getPrecioCompra());
                }

                detalle.setProductos(productoCompleto);
                detalle.setCompra(compras);
                BigDecimal subtotal = detalle.getPrecioUnitario().multiply(detalle.getCantidad());
                detalle.setSubtotal(subtotal);
                acumuladorSubtotales = acumuladorSubtotales.add(subtotal);
                detallesValidos.add(detalle);
            }

            if (detallesValidos.isEmpty()) {
                model.addAttribute("error", "Debe agregar al menos un producto válido con cantidad mayor a cero");
                model.addAttribute("proveedores", proveedorServicio.listarproveedores());
                model.addAttribute("productos", productoServicio.listarProductos());
                return "viewCompras/crearCompras";
            }

            compras.setDetalles(detallesValidos);


            BigDecimal totalFinal = acumuladorSubtotales;
            compras.setTotal(totalFinal);

            compras.setEstado(EstadoCompra.BORRADOR);
            compraServicio.saveCompra(compras);

            redirectAttributes.addFlashAttribute("success", "Compra creada con éxito en estado BORRADOR");
            return "redirect:/compras/listar";

        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la compra: " + e.getMessage());
            model.addAttribute("proveedores", proveedorServicio.listarproveedores());
            model.addAttribute("productos", productoServicio.listarProductos());
            return "viewCompras/crearCompras";
        }
    }


    @GetMapping("/editar/{id}")
    public String MostrarformularioEditar(@PathVariable Long id, Model model){
        Compras compras = compraServicio.compraById(id);
        model.addAttribute("compras",compras);
        model.addAttribute("proveedores",proveedorServicio.listarproveedores());
        model.addAttribute("productos",productoServicio.listarProductos());
        return "viewCompras/editarCompras";

    }


    @PostMapping("/editar/compra/{id}")
    public String editarCompra(@PathVariable Long id,
                               @Valid @ModelAttribute("compras") Compras compras, BindingResult result, RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal UserDetails userDetails) {

        if (result.hasErrors()) {
            return "viewCompras/editarCompras";
        }

        Usuario user = servicioUsuario.findByEmail(userDetails.getUsername());
        compras.setUsuario(user);
        try {
            // 1. Verificar que la compra existe y que aún se puede editar (Solo si está en BORRADOR)
            Compras compraExistente = compraServicio.compraById(id);

            if (compraExistente.getEstado() != EstadoCompra.BORRADOR) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede editar una compra que ya ha sido CONFIRMADA o ANULADA.");
                return "redirect:/compras/listar";
            }

            // 2. Actualizar datos básicos
            compraExistente.setProveedor(proveedorServicio.proveedorById(compras.getProveedor().getId()));


            // 3. Gestionar los Detalles: Limpiamos los anteriores y agregamos los nuevos
            // (Esto funciona gracias al orphanRemoval = true en la entidad Compras)
            compraExistente.getDetalles().clear();

            BigDecimal acumuladorSubtotales = BigDecimal.ZERO;

            for (DetalleCompra nuevoDetalle : compras.getDetalles()) {
                if (nuevoDetalle.getProductos() != null && nuevoDetalle.getProductos().getId() != null
                        && nuevoDetalle.getCantidad() != null && nuevoDetalle.getCantidad().compareTo(BigDecimal.ZERO) > 0) {

                    Productos prod = productoServicio.productoById(nuevoDetalle.getProductos().getId());
                    nuevoDetalle.setProductos(prod);
                    nuevoDetalle.setCompra(compraExistente); // Vinculamos al padre

                    BigDecimal subtotal = nuevoDetalle.getPrecioUnitario().multiply(nuevoDetalle.getCantidad());
                    nuevoDetalle.setSubtotal(subtotal);

                    acumuladorSubtotales = acumuladorSubtotales.add(subtotal);
                    compraExistente.getDetalles().add(nuevoDetalle);
                }
            }

            // 4. Recalcular Total Final
            compraExistente.setTotal(acumuladorSubtotales);
            // 5. Guardar cambios en Neon
            compraServicio.updateCompra(id,compraExistente);

            redirectAttributes.addFlashAttribute("success", "Compra actualizada correctamente");
            return "redirect:/compras/listar";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al editar: " + e.getMessage());
            return "redirect:/compras/listar";
        }
    }

    @GetMapping("/compra/delete/{id}")
    public String deleteCompra(@PathVariable Long id,RedirectAttributes redirectAttributes){
        compraServicio.deleteCompraById(id);
        redirectAttributes.addFlashAttribute("success", "Compra eliminada correctamente");
        return "redirect:/compras/listar";
    }

    @GetMapping("/confirmar/{id}")
    public String confirmCompra(@PathVariable Long id,RedirectAttributes redirectAttributes){
        try {
            Compras compra = compraServicio.compraById(id);
            String referencia = compraServicio.GenerarReferenciasDeCompras();
            compra.setNumeroReferencia(referencia);
            compraServicio.ConfirmarCompra(compra.getId());
            redirectAttributes.addFlashAttribute("success", "Compra confirmada correctamente");
            return "redirect:/compras/listar";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al confirmar la compra: " + e.getMessage());
            return "redirect:/compras/listar";
        }
    }

    @GetMapping("/anular/{id}")
    public String AnularCompra(@PathVariable Long id,RedirectAttributes redirectAttributes){
        try{

            Compras compra = compraServicio.compraById(id);
            String referencia = compraServicio.GenerarReferenciasDeCompras();
            compra.setNumeroReferencia(referencia);
            compraServicio.AnularCompra(compra.getId());
            redirectAttributes.addFlashAttribute("success", "Compra anulada correctamente");
            return "redirect:/compras/listar";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al anular la compra: " + e.getMessage());
            return "redirect:/compras/listar";
        }
    }

}
