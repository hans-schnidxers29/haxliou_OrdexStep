package com.example.demo.controlador;

import com.example.demo.servicio.VentaServicio;
import com.example.demo.entidad.*;
import com.example.demo.servicio.ServicioUsuario;
import com.example.demo.servicio.ServicioEmpresa;
import com.example.demo.entidad.DetalleVenta;
import com.example.demo.pdf.PdfServicio;
import com.example.demo.servicio.CajaServicio;
import com.example.demo.servicio.CategoriaService;
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
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

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

    @Autowired
    private CajaServicio cajaServicio;

    @Autowired
    private CategoriaService categoriaService;


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

        // Para sumaproductos (Unidades)
        model.addAttribute("sumaproductos", Optional.ofNullable(servicio.sumaproductosPordia())
                .flatMap(list -> list.stream().findFirst())
                .map(obj -> (Number) obj[0])
                .orElse(0));

        // Para SumaPorDias (Dinero/Peso)
        model.addAttribute("SumaPorDias", Optional.ofNullable(servicio.sumaproductosPordia())
                .flatMap(list -> list.stream().findFirst())
                .map(obj -> (BigDecimal) obj[1])
                .orElse(BigDecimal.ZERO));

        model.addAttribute("nombresProductos",servicio.NombreProductos());
        model.addAttribute("conteoProductos",servicio.CantidadProductos());
        return "ViewVentas/index";
    }

    // ============================
    // FORMULARIO CREAR VENTA
    // ============================

    @GetMapping("/crear")
    public String crearVentaMostrarForm(Model model,
                                        @AuthenticationPrincipal UserDetails userDetails) {

        Usuario usuario = servicioUsuario.findByEmail(userDetails.getUsername());

        // 🔑 OBTENER CAJA ABIERTA
        Caja cajaActiva = cajaServicio.CajaAbierta(usuario);

        // 🔑 ENVIAR AMBAS COSAS
        model.addAttribute("cajaAbierta", cajaActiva);
        model.addAttribute("necesitaAbrirCaja", cajaActiva == null);
        model.addAttribute("caja",cajaActiva);


        Venta venta = new Venta();
        venta.setCliente(new Cliente());
        venta.setDetalles(new ArrayList<>());

        DetalleVenta det = new DetalleVenta();
        det.setProducto(new Productos());
        det.setVenta(venta);
        if (cajaActiva != null) {
            Map<String, Object> resumen = cajaServicio.obtenerResumenActual(cajaActiva.getId());
            model.addAttribute("resumen", resumen);
        }

        model.addAttribute("clientes", clienteService.clienteSimple());
        model.addAttribute("productos", productoServicio.listarProductos());
        model.addAttribute("categorias",categoriaService.Listarcategoria());
        model.addAttribute("venta", venta);

        return "ViewVentas/crearVenta";
    }

    // ============================
    // GUARDAR NUEVA VENTA
    // ============================
    @PostMapping("/crear/nueva")
    public String CrearVenta(@ModelAttribute("ventas") Venta venta,
                             RedirectAttributes redirectAttributes,
                             Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 1. Validaciones de integridad
            if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "Debe agregar productos a la venta");
                return "redirect:/ventas/crear";
            }

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
            venta.setFechaVenta(LocalDateTime.now());

            // 2. Procesamiento de Detalles e Impuestos
            List<DetalleVenta> detallesValidos = new ArrayList<>();
            BigDecimal subtotalGeneral = BigDecimal.ZERO;
            BigDecimal totalImpuestosAcumulado = BigDecimal.ZERO;

            for (DetalleVenta detalle : venta.getDetalles()) {
                // Validar datos básicos del detalle
                if (detalle.getProducto() == null || detalle.getProducto().getId() == null ||
                        detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                Productos productoCompleto = productoServicio.productoById(detalle.getProducto().getId());
                if (productoCompleto == null) {
                    redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                    return "redirect:/ventas/crear";
                }

                // --- LÓGICA DE CONVERSIÓN (Gramos a Kilos si es PESO) ---
                BigDecimal cantidadOriginal = detalle.getCantidad();
                BigDecimal cantidadProcesada = "KGM".equals(productoCompleto.getTipoVenta().getCode())
                        ? cantidadOriginal.divide(new BigDecimal("1000")).setScale(3, RoundingMode.HALF_UP)
                        : cantidadOriginal;

                // --- VALIDACIONES DE STOCK ---
                if (productoCompleto.getCantidad().compareTo(cantidadProcesada) < 0) {
                    redirectAttributes.addFlashAttribute("error",
                            String.format("Stock insuficiente para '%s'. Disponible: %s %s",
                                    productoCompleto.getNombre(),
                                    productoCompleto.getCantidad(),
                                    productoCompleto.getTipoVenta()));
                    return "redirect:/ventas/crear";
                }

                // --- CÁLCULOS FINANCIEROS POR LÍNEA ---
                detalle.setProducto(productoCompleto);
                detalle.setVenta(venta);
                detalle.setCantidad(cantidadProcesada);

                if (detalle.getPrecioUnitario() == null) {
                    detalle.setPrecioUnitario(productoCompleto.getPrecio());
                }
                BigDecimal PrecioAProcesar = BigDecimal.ZERO;
                BigDecimal subtotalFila = BigDecimal.ZERO;

                if(Boolean.TRUE.equals(venta.getVentaAlPorMayor()) && productoCompleto.getPrecioPorMayor() != null){
                    PrecioAProcesar = productoCompleto.getPrecioPorMayor();
                    venta.setVentaAlPorMayor(true);
                }else{
                    // Subtotal de la fila (Precio x Cantidad)
                    PrecioAProcesar = productoCompleto.getPrecio();
                }

                detalle.setPrecioUnitario(PrecioAProcesar);
                subtotalFila = PrecioAProcesar.multiply(cantidadProcesada);
                detalle.setSubtotal(subtotalFila);

                // Cálculo de Impuesto de la fila (Subtotal * %Impuesto / 100)
                BigDecimal porcentajeImp = productoCompleto.getImpuesto() != null ? productoCompleto.getImpuesto() : BigDecimal.ZERO;
                BigDecimal impuestoFila = subtotalFila.multiply(porcentajeImp).divide(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);

                // --- ACUMULACIÓN DE TOTALES ---
                subtotalGeneral = subtotalGeneral.add(subtotalFila);
                totalImpuestosAcumulado = totalImpuestosAcumulado.add(impuestoFila).setScale(2, RoundingMode.HALF_UP);

                detallesValidos.add(detalle);
            }

            if (detallesValidos.isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "Debes agregar productos válidos");
                return "redirect:/ventas/crear";
            }

            // 3. Finalización de la Venta
            venta.setDetalles(detallesValidos);
            venta.setSubtotal(subtotalGeneral.setScale(2, RoundingMode.HALF_UP));

            // CORRECCIÓN: Guardar el PORCENTAJE de impuesto global (puedes tomarlo del primer producto o un promedio)
            // Si manejas un IVA estándar (ej. 19%), guárdalo como valor fijo.
            // Aquí lo calculamos de forma segura basándonos en los productos vendidos:
            BigDecimal porcentajeGlobal = BigDecimal.ZERO;
            if (!detallesValidos.isEmpty()) {
                porcentajeGlobal = detallesValidos.get(0).getProducto().getImpuesto();
            }
            venta.setImpuesto(porcentajeGlobal != null ? porcentajeGlobal : BigDecimal.ZERO); 

            // El Total sigue siendo Subtotal + Dinero del Impuesto Acumulado
            venta.setTotal(subtotalGeneral.add(totalImpuestosAcumulado).setScale(2, RoundingMode.HALF_UP));

            // 4. Gestión de Usuario y Stock
            Usuario usuarioVendedor = servicioUsuario.findByEmail(userDetails.getUsername());
            if (usuarioVendedor == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario vendedor no identificado");
                return "redirect:/ventas/crear";
            }
            venta.setVendedor(usuarioVendedor);

            // Actualizar inventario
            servicio.DescontarStock(venta);

            // Persistir en Base de Datos
            servicio.guardarVenta(venta);

            redirectAttributes.addFlashAttribute("success", "Venta creada exitosamente. Total: $" + venta.getTotal());
            return "redirect:/ventas/listar";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error crítico al procesar venta: " + e.getMessage());
            return "redirect:/ventas/crear";
        }
    }

    // ============================
    // PDF TICKET DE VENTA
    // ============================
    @GetMapping("/ticket/{id}")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id,@AuthenticationPrincipal UserDetails usuario) throws Exception {

        Usuario user = servicioUsuario.findByEmail(usuario.getUsername());
        Long IdEmpresa  = servicioUsuario.ObtenreIdEmpresa(user.getId());
        Venta venta = servicio.buscarVenta(id);

        // Usamos tu método DatosEmpresa con ID 1
        Empresa empresa = servicioEmpresa.DatosEmpresa(IdEmpresa);

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
