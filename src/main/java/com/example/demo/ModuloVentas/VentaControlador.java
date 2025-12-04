package com.example.demo.ModuloVentas;

import com.example.demo.ModuloVentas.DetalleVenta.DetalleVenta;
import com.example.demo.entidad.Cliente;
import com.example.demo.entidad.Productos;
import com.example.demo.pdf.PdfServicio;
import com.example.demo.servicio.ClienteService;
import com.example.demo.servicio.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/ventas")
public class VentaControlador {

    @Autowired
    private VentaServicio servicio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private ClienteService clienteService;

    private PdfServicio pdfServicio;
    // Inyectar el servicio
    public VentaControlador(PdfServicio pdfServicio) {
        this.pdfServicio = pdfServicio;
    }
    // ============================
    // LISTAR VENTAS
    // ============================
    @GetMapping("/listar")
    public String listar(Model model) {
        model.addAttribute("ventas", servicio.ListarVenta());
        return "ViewVentas/index";
    }

    // ============================
    // FORMULARIO CREAR VENTA
    // ============================
    @GetMapping("/crear")
    public String crearVentaMostrarForm(Model model) {

        Venta venta = new Venta();

        // Cliente vacío
        venta.setCliente(new Cliente());

        // Lista inicializada
        venta.setDetalles(new ArrayList<>());

        // Agregar un detalle vacío
        DetalleVenta det = new DetalleVenta();
        det.setProducto(new Productos());
        det.setVenta(venta);
        venta.getDetalles().add(det);

        model.addAttribute("clientes", clienteService.listarcliente());
        model.addAttribute("productos", productoServicio.listarProductos());
        model.addAttribute("venta", venta);

        return "ViewVentas/crearVenta";
    }

    // ============================
    // GUARDAR NUEVA VENTA
    // ============================
    @PostMapping("/crear/nueva")
    public String CrearVenta(@ModelAttribute("venta") Venta venta,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        try {

            // Validar lista de detalles
            if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "Debe agregar productos a la venta");
                return "redirect:/ventas/crear?error=true";
            }

            // Validar cliente
            if (venta.getCliente() == null || venta.getCliente().getId() == null) {
                redirectAttributes.addFlashAttribute("mensaje", "Debe seleccionar un cliente");
                return "redirect:/ventas/crear?error=true";
            }

            Cliente clienteCompleto = clienteService.clientdById(venta.getCliente().getId());
            if (clienteCompleto == null) {
                redirectAttributes.addFlashAttribute("mensaje", "Cliente no encontrado");
                return "redirect:/ventas/crear?error=true";
            }
            venta.setCliente(clienteCompleto);

            // PROCESAR DETALLES
            List<DetalleVenta> detallesValidos = new ArrayList<>();
            BigDecimal subtotalVenta = BigDecimal.ZERO;

            for (DetalleVenta detalle : venta.getDetalles()) {

                if (detalle.getProducto() == null ||
                        detalle.getProducto().getId() == null ||
                        detalle.getCantidad() == null ||
                        detalle.getCantidad() <= 0) {
                    continue;
                }

                Productos productoCompleto = productoServicio.productoById(detalle.getProducto().getId());
                if (productoCompleto == null) {
                    redirectAttributes.addFlashAttribute("mensaje",
                            "Producto no encontrado");
                    return "redirect:/ventas/crear?error=true";
                }

                // Stock
                if (productoCompleto.getCantidad() < detalle.getCantidad()) {
                    redirectAttributes.addFlashAttribute("mensaje",
                            "Stock insuficiente para " + productoCompleto.getNombre());
                    return "redirect:/ventas/crear?error=true";
                }

                detalle.setProducto(productoCompleto);
                detalle.setVenta(venta);

                // Precio unitario
                if (detalle.getPrecioUnitario() == null) {
                    detalle.setPrecioUnitario(productoCompleto.getPrecio());
                }

                // Subtotal
                BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
                detalle.setSubtotal(detalle.getPrecioUnitario().multiply(cantidad));

                subtotalVenta = subtotalVenta.add(detalle.getSubtotal());

                detallesValidos.add(detalle);
            }

            // Sin productos válidos
            if (detallesValidos.isEmpty()) {
                redirectAttributes.addFlashAttribute("mensaje", "Debes agregar productos válidos");
                return "redirect:/ventas/crear?error=true";
            }

            venta.setDetalles(detallesValidos);
            venta.setSubtotal(subtotalVenta);

            // Impuesto
            if (venta.getImpuesto() == null) {
                venta.setImpuesto(BigDecimal.ZERO);
            }

            venta.setTotal(subtotalVenta.add(venta.getImpuesto()));
            //descontar del stock
            servicio.DescontarStock(venta);
            // Guardar
            servicio.guardarVenta(venta);

            redirectAttributes.addFlashAttribute("mensaje", "Venta creada exitosamente");
            return "redirect:/ventas/listar?success=true";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("mensaje", "Error al crear la venta");
            return "redirect:/ventas/crear?error=true";
        }
    }

    // ============================
    // PDF TICKET DE VENTA
    // ============================
    @GetMapping("/ticket/{id}")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id) throws Exception {

        Venta venta = servicio.buscarVenta(id);

        BigDecimal subtotal = venta.getDetalles()
                .stream()
                .map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal impuesto = venta.getTotal().subtract(subtotal);

        Map<String, Object> datos = new HashMap<>();
        datos.put("venta", venta);
        datos.put("subtotal", subtotal);
        datos.put("impuesto", impuesto);

        byte[] pdf = pdfServicio.generarPdf("pdf/ticketVenta", datos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ticket_venta_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
