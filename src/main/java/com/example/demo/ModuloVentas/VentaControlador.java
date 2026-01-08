package com.example.demo.ModuloVentas;

import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Empresa;
import com.example.demo.Login.Servicio.ServicioEmpresa;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Autowired
    private ServicioEmpresa servicioEmpresa;

    @Autowired
    private ServicioUsuario servicioUsuario;


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
        model.addAttribute("totalRecaudado",servicio.totalVentas());
        model.addAttribute("sumaproductos",servicio.sumaproductos());
        model.addAttribute("SumaPorDias", servicio.sumaproductosPordia());
        List<String> etiquetas = servicio.ListaMeses();
        List<BigDecimal> valores = servicio.listarTotalVentas();
        model.addAttribute("labelsGrafica", etiquetas);
        model.addAttribute("datosGrafica", valores );
        model.addAttribute("nombresProductos",servicio.NombreProductos());
        model.addAttribute("conteoProductos",servicio.CantidadProductos());
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
                             Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Validar lista de detalles
            if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "Debe agregar productos a la venta");
                return "redirect:/ventas/crear";
            }

            // Validar cliente
            if (venta.getCliente() == null || venta.getCliente().getId() == null) {
                redirectAttributes.addFlashAttribute("info", "Debe seleccionar un cliente");
                return "redirect:/ventas/crear";
            }

            Cliente clienteCompleto = clienteService.clientdById(venta.getCliente().getId());
            if (clienteCompleto == null) {
                redirectAttributes.addFlashAttribute("info", "Cliente no encontrado");
                return "redirect:/ventas/crear";
            }
            venta.setCliente(clienteCompleto);

            // PROCESAR DETALLES
            List<DetalleVenta> detallesValidos = new ArrayList<>();
            BigDecimal subtotalVenta = BigDecimal.ZERO;

            for (DetalleVenta detalle : venta.getDetalles()) {

                if (detalle.getProducto() == null ||
                        detalle.getProducto().getId() == null ||
                        detalle.getCantidad() == null ||
                        detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                Productos productoCompleto = productoServicio.productoById(detalle.getProducto().getId());
                if (productoCompleto == null) {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/ventas/crear";
                }

                // --- LÓGICA DE CONVERSIÓN GRAMOS A KILOS ---
                BigDecimal cantidadOriginal = detalle.getCantidad(); 
                BigDecimal cantidadProcesada = cantidadOriginal;

                // Si el producto se vende por PESO, convertimos los gramos recibidos a KG
                if ("PESO".equals(productoCompleto.getTipoVenta().name())) {
                    cantidadProcesada = cantidadOriginal.divide(new BigDecimal("1000"));
                    // Seteamos la cantidad convertida al detalle para que el stock se descuente correctamente
                    detalle.setCantidad(cantidadProcesada);
                }

                // VALIDACIÓN 1: Cantidad mínima de venta (comparando en la unidad base: KG)
                if (productoCompleto.getCantidadMinima() != null &&
                        cantidadProcesada.compareTo(productoCompleto.getCantidadMinima()) < 0) {
                    redirectAttributes.addFlashAttribute("warning",
                            String.format("La cantidad mínima para '%s' es %s %s",
                                    productoCompleto.getNombre(),
                                    productoCompleto.getCantidadMinima(),
                                    productoCompleto.getUnidadMedida()));
                    return "redirect:/ventas/crear";
                }

                // ✅ VALIDACIÓN 2: Stock suficiente (comparando KG disponibles vs KG solicitados)
                if (productoCompleto.getCantidad().compareTo(cantidadProcesada) < 0) {
                    redirectAttributes.addFlashAttribute("error",
                            String.format("Stock insuficiente para '%s'. Disponible: %s %s",
                                    productoCompleto.getNombre(),
                                    productoCompleto.getCantidad(),
                                    productoCompleto.getUnidadMedida()));
                    return "redirect:/ventas/crear";
                }

                detalle.setProducto(productoCompleto);
                detalle.setVenta(venta);

                // Precio unitario (Precio por KG o por Unidad)
                if (detalle.getPrecioUnitario() == null) {
                    detalle.setPrecioUnitario(productoCompleto.getPrecio());
                }

                // Subtotal: Precio * Cantidad (si es peso, ya es Precio_KG * Cantidad_KG)
                detalle.setSubtotal(detalle.getPrecioUnitario().multiply(cantidadProcesada));

                subtotalVenta = subtotalVenta.add(detalle.getSubtotal());
                detallesValidos.add(detalle);
            }

            // Sin productos válidos
            if (detallesValidos.isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "Debes agregar productos válidos");
                return "redirect:/ventas/crear";
            }

            venta.setDetalles(detallesValidos);
            venta.setSubtotal(subtotalVenta);

            // Impuesto
            if (venta.getImpuesto() == null) {
                venta.setImpuesto(BigDecimal.ZERO);
            }

            // El total es Subtotal + Impuesto (Asumiendo que el impuesto es un valor fijo, no porcentaje)
            // Si el impuesto en tu objeto Venta es porcentaje, deberías calcularlo antes.
            venta.setTotal(subtotalVenta.add(venta.getImpuesto()));

            // Descontar del stock (Usa las cantidades ya convertidas en los detalles)
            servicio.DescontarStock(venta);

            // extraemos el email del vendedor para mapear en la db
            Usuario usuarioVendedor = servicioUsuario.findByEmail(userDetails.getUsername());
            if(usuarioVendedor == null){
                redirectAttributes.addFlashAttribute("error", "Usuario no encontrado");
                return "redirect:/ventas/crear";
            }
            venta.setVendedor(usuarioVendedor);

            // Guardar
            servicio.guardarVenta(venta);

            redirectAttributes.addFlashAttribute("success", "Venta creada exitosamente");
            return "redirect:/ventas/listar";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al crear la venta: " + e.getMessage());
            return "redirect:/ventas/crear";
        }
    }

    // ============================
    // PDF TICKET DE VENTA
    // ============================
    @GetMapping("/ticket/{id}")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id) throws Exception {

        Venta venta = servicio.buscarVenta(id);

        // Usamos tu método DatosEmpresa con ID 1
        Empresa empresa = servicioEmpresa.DatosEmpresa(1L);

        BigDecimal subtotal = venta.getSubtotal();
        BigDecimal impuesto = venta.getTotal().subtract(subtotal);

        Map<String, Object> datos = new HashMap<>();
        datos.put("venta", venta);
        datos.put("subtotal", subtotal);
        datos.put("impuesto", impuesto);
        datos.put("empresa", empresa); // Pasamos el objeto empresa completo

        byte[] pdf = pdfServicio.generarPdf("pdf/ticketVenta", datos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ticket_venta_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
