package com.example.demo.controlador;

import com.example.demo.servicio.VentaServicio;
import com.example.demo.entidad.*;
import com.example.demo.servicio.ServicioUsuario;
import com.example.demo.servicio.ServicioEmpresa;
import com.example.demo.entidad.DetalleVenta;
import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.Venta;
import com.example.demo.entidad.Usuario;
import com.example.demo.entidad.Caja;
import com.example.demo.entidad.Empresa;
import com.example.demo.entidad.Cliente;
import com.example.demo.entidad.Productos;
import com.example.demo.pdf.PdfServicio;
import com.example.demo.servicio.CajaServicio;
import com.example.demo.servicio.CategoriaService;
import com.example.demo.servicio.ClienteService;
import com.example.demo.servicio.ProductoServicio;
import com.example.demo.util.RoundingUtil;
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
import java.util.stream.Collectors;

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

    @Autowired private SecurityService securityService;


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


            //Logica de valores Adicionales
            BigDecimal valorAdicional = venta.getValoresAdicionales();
            if(valorAdicional == null) {
                venta.setValoresAdicionales(BigDecimal.ZERO);
                valorAdicional = BigDecimal.ZERO;
            }else{
                venta.setValoresAdicionales(valorAdicional);
            }

            //Logica de Descuento
            BigDecimal TotalDescuento = BigDecimal.ZERO;
            BigDecimal descuentoProcesar = venta.getDescuento();
            if(descuentoProcesar == null){
                venta.setDescuento(BigDecimal.ZERO);
            }else{
                TotalDescuento = descuentoProcesar.multiply(subtotalGeneral)
                        .divide(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
                venta.setDescuento(descuentoProcesar);
            }


            BigDecimal porcentajeGlobal = BigDecimal.ZERO;
            if (!detallesValidos.isEmpty()) {
                porcentajeGlobal = detallesValidos.get(0).getProducto().getImpuesto();
            }
            venta.setImpuesto(porcentajeGlobal != null ? porcentajeGlobal : BigDecimal.ZERO);

            // El Total sigue siendo Subtotal + Dinero del Impuesto Acumulado
            BigDecimal totalCalculado = subtotalGeneral.add(totalImpuestosAcumulado).subtract(TotalDescuento).add(valorAdicional);
            venta.setTotal(RoundingUtil.roundToColombianPeso(totalCalculado));

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

    @GetMapping("/editar/{id}")
    public String MostrarFormEditar(@PathVariable Long id,Model model, RedirectAttributes flash){
        try {
            Venta venta = servicio.buscarVenta(id);
            if (venta == null) {
                flash.addFlashAttribute("info", "venta no encontrada");
                return "redirect:/ventas/listar";
            }
            if(venta.getCliente() == null || venta.getDetalles() ==  null){
                flash.addFlashAttribute("info","productos no encotrados o requiere cliente");
                return "redirect:/vebtas/listar";
            }
            model.addAttribute("clientes", clienteService.clienteSimple());
            model.addAttribute("productos", productoServicio.listarProductos());
            model.addAttribute("categorias", categoriaService.Listarcategoria());
            model.addAttribute("venta", venta);
            return "ViewVentas/VentasEditar";

        }catch(Exception e ){
            flash.addFlashAttribute("error", "error al editar venta "+ e.getMessage());
            return "redirect:/ventas/listar";
        }
    }

    @PostMapping("/editar/venta/{id}")
    public String EditarVenta(@PathVariable Long id, @ModelAttribute("venta") Venta venta,
                              RedirectAttributes flash, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // 1. Cargar la venta original para gestionar Stock y Detalles antiguos
            Venta ventaExistente = servicio.buscarVenta(id);
            if (ventaExistente == null) {
                flash.addFlashAttribute("error", "La venta no existe.");
                return "redirect:/ventas/listar";
            }

            // Devolver el stock original antes de validar el nuevo
            // Esto es vital: si el usuario compró 5 y ahora quiere 3, primero devolvemos los 5 al inventario.
            for (DetalleVenta antiguo : ventaExistente.getDetalles()) {
                productoServicio.AgregarStock(antiguo.getProducto().getId(), antiguo.getCantidad(),BigDecimal.ZERO,BigDecimal.ZERO);
            }

            // 2. Validaciones básicas de la nueva data
            if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
                flash.addFlashAttribute("info", "Debe agregar productos a la venta");
                return "redirect:/ventas/editar/" + id;
            }

            if (venta.getCliente() == null || venta.getCliente().getId() == null) {
                flash.addFlashAttribute("info", "Debe seleccionar un cliente");
                return "redirect:/ventas/editar/" + id;
            }

            Cliente clienteCompleto = clienteService.clientdById(venta.getCliente().getId());
            venta.setCliente(clienteCompleto);
            // Mantener datos originales que no cambian
            venta.setId(id);
            venta.setFechaVenta(ventaExistente.getFechaVenta()); // Mantener fecha original o LocalDateTime.now() si prefieres fecha de edición
            venta.setEmpresa(ventaExistente.getEmpresa());

            // 3. Procesamiento de Detalles
            BigDecimal subtotalFormulario = venta.getSubtotal(); // Capturamos el valor del formulario
            List<DetalleVenta> nuevosDetalles = new ArrayList<>();
            BigDecimal subtotalGeneral = BigDecimal.ZERO;
            BigDecimal totalImpuestosAcumulado = BigDecimal.ZERO;

            for (DetalleVenta detalle : venta.getDetalles()) {
                if (detalle.getProducto() == null || detalle.getProducto().getId() == null ||
                        detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                Productos producto = productoServicio.productoById(detalle.getProducto().getId());

                // Lógica de conversión Gramos a Kilos
                BigDecimal cantidadOriginal = detalle.getCantidad();
                BigDecimal cantidadProcesada = "KGM".equals(producto.getTipoVenta().getCode())
                        ? cantidadOriginal.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP)
                        : cantidadOriginal;

                // Validar Stock (Ahora que ya devolvimos lo de la venta anterior)
                if (producto.getCantidad().compareTo(cantidadProcesada) < 0) {
                    flash.addFlashAttribute("error", "Stock insuficiente para " + producto.getNombre());
                    return "redirect:/ventas/editar/" + id;
                }

                // Descontar nuevo Stock
                servicio.descontarStock(venta);

                // Precios y Subtotales
                // Precios y Subtotales
                BigDecimal precioAplicado = detalle.getPrecioUnitario();

                // Si no viene precio, calculamos según reglas de negocio (Mayorista/Detal)
                if (precioAplicado == null) {
                     precioAplicado = (Boolean.TRUE.equals(venta.getVentaAlPorMayor()) && producto.getPrecioPorMayor() != null)
                            ? producto.getPrecioPorMayor()
                            : producto.getPrecio();
                }

                BigDecimal subtotalFila = precioAplicado.multiply(cantidadProcesada);
                BigDecimal porcentajeImp = producto.getImpuesto() != null ? producto.getImpuesto() : BigDecimal.ZERO;
                BigDecimal impuestoFila = subtotalFila.multiply(porcentajeImp).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                detalle.setVenta(venta);
                detalle.setProducto(producto);
                detalle.setCantidad(cantidadProcesada);
                detalle.setPrecioUnitario(precioAplicado);
                detalle.setSubtotal(subtotalFila);

                subtotalGeneral = subtotalGeneral.add(subtotalFila);
                totalImpuestosAcumulado = totalImpuestosAcumulado.add(impuestoFila);
                nuevosDetalles.add(detalle);
            }

            // 4. Totales Finales
            venta.setDetalles(nuevosDetalles);

            // Lógica para respetar el subtotal manual si el usuario lo editó
            if (subtotalFormulario != null && subtotalFormulario.compareTo(BigDecimal.ZERO) > 0) {
                 venta.setSubtotal(subtotalFormulario);
                 subtotalGeneral = subtotalFormulario; // Usamos este valor para el cálculo de descuentos/total
            } else {
                 venta.setSubtotal(subtotalGeneral.setScale(2, RoundingMode.HALF_UP));
            }

            // Valores adicionales y Descuentos
            BigDecimal adicionales = venta.getValoresAdicionales() != null ? venta.getValoresAdicionales() : BigDecimal.ZERO;
            BigDecimal descPorcentaje = venta.getDescuento() != null ? venta.getDescuento() : BigDecimal.ZERO;
            BigDecimal valorDescuento = subtotalGeneral.multiply(descPorcentaje).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

            BigDecimal totalCalculado = subtotalGeneral.add(totalImpuestosAcumulado).subtract(valorDescuento).add(adicionales);
            venta.setTotal(RoundingUtil.roundToColombianPeso(totalCalculado));
            venta.setImpuesto(totalImpuestosAcumulado); // Guardamos el valor monetario del impuesto acumulado

            // 5. Guardar Cambios
            Usuario vendedor = servicioUsuario.findByEmail(userDetails.getUsername());
            venta.setVendedor(vendedor);

            // Limpiar detalles antiguos en la DB antes de guardar los nuevos (Orphan Removal)
            servicio.UpdateVenta(venta);

            flash.addFlashAttribute("success", "Venta actualizada correctamente");
            return "redirect:/ventas/listar";

        } catch (Exception e) {
            flash.addFlashAttribute("error", "Error al editar la venta: " + e.getMessage());
            return "redirect:/ventas/listar";
        }
    }






    // ============================
    // PDF TICKET DE VENTA
    // ============================
    @GetMapping("/ticket/{id}")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id, @AuthenticationPrincipal UserDetails usuarioLogueado) throws Exception {

        Venta venta = servicio.buscarVenta(id);
        Empresa empresa = securityService.ObtenerEmpresa();

        BigDecimal subtotal = venta.getSubtotal();
        BigDecimal impuesto = venta.getImpuesto(); // O el cálculo que ya tienes

        // Cálculo del valor del descuento basado en el porcentaje guardado en la venta
        BigDecimal porcentajeDescuento = (venta.getDescuento() != null) ? venta.getDescuento() : BigDecimal.ZERO;
        BigDecimal valorDescuento = subtotal.multiply(porcentajeDescuento).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        Map<String, Object> datos = new HashMap<>();
        datos.put("venta", venta);
        datos.put("subtotal", subtotal);
        datos.put("impuesto", impuesto);
        datos.put("valorDescuento", valorDescuento);
        datos.put("porcentajeDescuento", porcentajeDescuento);
        datos.put("empresa", empresa);
        // Accedemos al nombre del vendedor desde la relación de la venta
        datos.put("vendedorNombre", venta.getVendedor().getNombre() + " " + (venta.getVendedor().getApellido() != null ? venta.getVendedor().getApellido() : ""));

        byte[] pdf = pdfServicio.generarPdf("pdf/ticketVenta", datos);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ticket_venta_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
