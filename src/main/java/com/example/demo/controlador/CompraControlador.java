package com.example.demo.controlador;

import com.example.demo.entidad.*;
import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.repositorio.ComprasCreditoRepositorio;
import com.example.demo.servicio.*;
import com.example.demo.entidad.Enum.EstadoCompra;
import com.example.demo.entidad.Enum.MetodoPago;
import groovyjarjarpicocli.CommandLine;
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

    @Autowired
    private ComprasCreditoRepositorio comprasCreditoRepo;

    @Autowired
    private FinanzasServicio finanzasServicio;


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
        model.addAttribute("productos",productoServicio.ProductoSimple());
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
            model.addAttribute("productos", productoServicio.ProductoSimple());
            return "viewCompras/crearCompras";
        }

        try {
            // 2. Asignar Usuario autenticado
            Usuario user = servicioUsuario.findByEmail(userDetails.getUsername());
            compras.setUsuario(user);

            // 3. Validar y Asignar el Proveedor seleccionado en el Select
            if (compras.getProveedor() == null || compras.getProveedor().getId() == null) {
                model.addAttribute("error", "Debe seleccionar un proveedor válido.");
                model.addAttribute("proveedores", proveedorServicio.listarproveedores());
                model.addAttribute("productos", productoServicio.ProductoSimple());
                return "viewCompras/crearCompras";
            }
            // Cargamos el proveedor completo desde la DB
            Proveedores proveedorSeleccionado = proveedorServicio.proveedorById(compras.getProveedor().getId());
            compras.setProveedor(proveedorSeleccionado);

            // 4. Procesar Detalles de Compra
            if (compras.getDetalles() == null || compras.getDetalles().isEmpty()) {
                model.addAttribute("info", "La lista de productos no puede estar vacía");
                model.addAttribute("proveedores", proveedorServicio.listarproveedores());
                model.addAttribute("productos", productoServicio.ProductoSimple());
                return "viewCompras/crearCompras";
            }

            List<DetalleCompra> detallesValidos = new ArrayList<>();
            BigDecimal acumuladorSubtotales = BigDecimal.ZERO;
            Proveedores primerProveedor = null; // To store the supplier of the first product

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
                model.addAttribute("productos", productoServicio.ProductoSimple());
                return "viewCompras/crearCompras";
            }

            compras.setDetalles(detallesValidos);
            BigDecimal totalFinal = acumuladorSubtotales;
            compras.setTotal(totalFinal);

            if(MetodoPago.CREDITO.equals(compras.getMetodoPago())){

                compras.setEstado(EstadoCompra.CREDITO);
                compraServicio.saveCompra(compras);

                ComprasCreditos comprasCreditos = new ComprasCreditos();
                comprasCreditos.setCompra(compras);
                comprasCreditos.setSaldoPendiente(compras.getTotal());
                comprasCreditos.setEstadoDeuda(EstadoPedido.PENDIENTE);
                comprasCreditos.setMontoTotal(compras.getTotal());
                comprasCreditos.setFechaVencimiento(LocalDate.now().plusDays(30));
                comprasCreditoRepo.save(comprasCreditos);

                for(DetalleCompra detalleCompra : detallesValidos){
                    BigDecimal nuevoImpuesto = detalleCompra.getProductos().getImpuesto();
                    BigDecimal nuevoPrecioCompra = detalleCompra.getProductos().getPrecioCompra();
                    productoServicio.AgregarStock(detalleCompra.getProductos().getId(),
                            detalleCompra.getCantidad(), nuevoImpuesto , nuevoPrecioCompra);
                }
                redirectAttributes.addFlashAttribute("success", "Compra a CRÉDITO registrada y stock actualizado.");
            }else{
                compras.setEstado(EstadoCompra.BORRADOR);
                compraServicio.saveCompra(compras);
                redirectAttributes.addFlashAttribute("success", "Compra creada con éxito en estado BORRADOR");
            }
            return "redirect:/compras/listar";

        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar la compra: " + e.getMessage());
            model.addAttribute("proveedores", proveedorServicio.listarproveedores());
            model.addAttribute("productos", productoServicio.ProductoSimple());
            return "viewCompras/crearCompras";
        }
    }


    @GetMapping("/editar/{id}")
    public String MostrarformularioEditar(@PathVariable Long id, Model model){
        Compras compras = compraServicio.compraById(id);
        model.addAttribute("compras",compras);
        model.addAttribute("proveedores",proveedorServicio.listarproveedores());
        model.addAttribute("productos",productoServicio.ProductoSimple());
        model.addAttribute("MetodoPago", MetodoPago.values());
        return "viewCompras/editarCompra";

    }


    @PostMapping("/editar/compra/{id}")
    public String editarCompra(@PathVariable Long id,
                               @ModelAttribute("compras") Compras compras,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               @AuthenticationPrincipal UserDetails userDetails) {

        try {
            // 1. Cargar la compra original de la DB
            Compras compraExistente = compraServicio.compraById(id);

            if (compraExistente.getEstado() != EstadoCompra.BORRADOR) {
                redirectAttributes.addFlashAttribute("error", "Solo se pueden editar compras en BORRADOR.");
                return "redirect:/compras/listar";
            }

            // 2. Datos básicos
            compraExistente.setMetodoPago(compras.getMetodoPago());
            compraExistente.setObservaciones(compras.getObservaciones());

            // 3. Gestionar Detalles
            compraExistente.getDetalles().clear(); // Requiere orphanRemoval = true en la Entidad

            if (compras.getDetalles() != null) {
                BigDecimal acumulador = BigDecimal.ZERO;
                Proveedores primerProveedor = null; // To store the supplier of the first product
                for (DetalleCompra detalle : compras.getDetalles()) {
                    if (detalle.getProductos() != null && detalle.getProductos().getId() != null) {

                        Productos prod = productoServicio.productoById(detalle.getProductos().getId());
                        // Set the supplier of the purchase from the first product found
                        if (primerProveedor == null && prod.getProveedor() != null) {
                            primerProveedor = prod.getProveedor();
                        }

                        detalle.setProductos(prod);
                        detalle.setCompra(compraExistente); // Vínculo necesario para JPA

                        BigDecimal subtotal = detalle.getPrecioUnitario().multiply(detalle.getCantidad());
                        detalle.setSubtotal(subtotal);
                        acumulador = acumulador.add(subtotal);

                        compraExistente.getDetalles().add(detalle);
                    }
                }
                // Set the purchase's supplier based on the first product's supplier
                if (primerProveedor != null) {
                    compraExistente.setProveedor(primerProveedor);
                } else {
                    compraExistente.setProveedor(null);
                }
                compraExistente.setTotal(acumulador);
            }

            // 4. Guardar
            compraServicio.updateCompra(id, compraExistente);

            redirectAttributes.addFlashAttribute("success", "Compra #" + id + " actualizada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }
        return "redirect:/compras/listar";
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
            compraServicio.AnularCompra(compra.getId());
            redirectAttributes.addFlashAttribute("success", "Compra anulada correctamente");
            return "redirect:/compras/listar";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al anular la compra: " + e.getMessage());
            return "redirect:/compras/listar";
        }
    }

    @GetMapping("/cuentas-por-pagar")
    public String listarCuentas(Model model){
        model.addAttribute("cuentas", comprasCreditoRepo.findAllBySaldoPendienteGreaterThan(BigDecimal.ZERO));
        model.addAttribute("metodosPago", MetodoPago.values());
        return "viewCompras/cuentasPorPagar";
    }

    @PostMapping("/registrar-abono")
    public String registrarAbono(@RequestParam Long cuentaId, @RequestParam BigDecimal monto, @RequestParam MetodoPago metodoPago,
                                 @RequestParam(defaultValue = "false") boolean afectaCaja, RedirectAttributes ra,
                                 @RequestParam(defaultValue = "0") BigDecimal montoEfectivo, @RequestParam(defaultValue = "0") BigDecimal montoTrans) {

        try {
            finanzasServicio.ProcesarAbono(cuentaId, monto, metodoPago, afectaCaja, montoEfectivo,montoTrans);
            ra.addFlashAttribute("success", "Abono registrado correctamente");
            return "redirect:/compras/cuentas-por-pagar";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/compras/cuentas-por-pagar";
        }

    }
}
