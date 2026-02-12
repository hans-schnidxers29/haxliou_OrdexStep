package com.example.demo.controlador;


import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.*;
import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.pdf.PdfServicio;
import com.example.demo.servicio.*;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/pedidos")
public class PedidosControlador {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoServicio productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ServicioEmpresa empresaService;

    private PdfServicio pdfService;

    public PedidosControlador(PdfServicio pdfService) {
        this.pdfService = pdfService;
    }

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ServicioUsuario servicoUser;

    @Autowired private SecurityService securityService;

    /**
     * Listar todos los pedidos
     */
    @GetMapping("/listarpedidos")  // ← AGREGAR la barra
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarpedidos());
        model.addAttribute("Estadisticas",pedidoService.ContarPorestados(EstadoPedido.PENDIENTE));
        model.addAttribute("Estadisticas2",pedidoService.estadoCEntregado(EstadoPedido.ENTREGADO));
        model.addAttribute("Estadisticas3",pedidoService.estadoCancelado(EstadoPedido.CANCELADO));
        List<Map<String, Object>> pendientesSimple = pedidoService.listarpedidos().stream()
                .filter(p -> p.getEstado() == EstadoPedido.PENDIENTE)
                .map(p -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", p.getId());
                    map.put("clienteNombre", p.getCliente() != null ? p.getCliente().getNombre() : "N/A");
                    map.put("fechaPedido", p.getFechaPedido() != null ? p.getFechaPedido().toString() : "");
                    map.put("total", p.getTotal());
                    return map;
                }).toList();
        model.addAttribute("pedidosPendientes", pendientesSimple);
        return "viewPedidos/index";
    }

    /**
     * Mostrar formulario para crear nuevo pedido
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("clientes", clienteService.clienteSimple());
        model.addAttribute("categorias",categoriaService.Listarcategoria());

        Pedidos pedido = new Pedidos();
        pedido.setCliente(new Cliente()); // Inicializar cliente vacío

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(new Productos()); // Inicializar producto vacío
        pedido.getDetalles().add(detalle);

        model.addAttribute("pedido", pedido);  // ← CAMBIAR a "pedido" (singular)
        return "viewPedidos/crearPedidos";
    }

    /**
     * Guardar nuevo pedido
     */
    @PostMapping("/crear")
    public String guardarPedido(@ModelAttribute("pedido") Pedidos pedido,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            if (pedido.getDetalles() != null) {
                pedido.getDetalles().removeIf(d -> d == null || d.getProducto() == null || d.getProducto().getId() == null);
            }

            // Validar que tenga detalles
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "El pedido debe tener al menos un producto válido");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.clienteSimple());
                model.addAttribute("categorias", categoriaService.Listarcategoria());
                model.addAttribute("pedido", pedido);
                return "redirect:/pedidos/nuevo";
            }

            // Validar que tenga detalles
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                redirectAttributes.addFlashAttribute("info", "El pedido debe tener al menos un producto");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.clienteSimple());
                model.addAttribute("categorias",categoriaService.Listarcategoria());
                model.addAttribute("pedido", pedido);
                return "redirect:/pedidos/nuevo";
            }

            // Validar cliente
            if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
                model.addAttribute("info", "Debes seleccionar un cliente");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.clienteSimple());
                model.addAttribute("categorias",categoriaService.Listarcategoria());
                model.addAttribute("pedido", pedido);
                return "redirect:/pedidos/nuevo";
            }

            // ✅ BUSCAR CLIENTE COMPLETO DESDE LA BD
            Cliente clienteCompleto = clienteService.clientdById(pedido.getCliente().getId());
            if (clienteCompleto == null) {
                model.addAttribute("info", "Cliente no encontrado");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.clienteSimple());
                model.addAttribute("categorias",categoriaService.Listarcategoria());
                model.addAttribute("pedido", pedido);
                return "redirect:/pedidos/nuevo";
            }
            pedido.setCliente(clienteCompleto);

            // ✅ BUSCAR PRODUCTOS COMPLETOS Y CALCULAR TOTALES
            BigDecimal subtotalPedido = BigDecimal.ZERO;
            BigDecimal montoTotalImpuestos = BigDecimal.ZERO;
            List<DetallePedido> detallesValidos = new ArrayList<>();

            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.getProducto() != null &&
                        detalle.getProducto().getId() != null &&
                        detalle.getCantidad() != null &&
                        detalle.getCantidad().compareTo(BigDecimal.ZERO) > 0) {

                    // Buscar producto completo desde la BD
                    Productos productoCompleto = productoService.productoById(detalle.getProducto().getId());

                    if (productoCompleto == null) {
                        redirectAttributes.addFlashAttribute("info", "Producto no encontrado");
                        model.addAttribute("productos", productoService.listarProductos());
                        model.addAttribute("clientes", clienteService.listarcliente());
                        model.addAttribute("categorias",categoriaService.Listarcategoria());
                        model.addAttribute("pedido", pedido);
                        return "redirect:/pedidos/nuevo";
                    }

                    // --- LÓGICA DE CONVERSIÓN (USA VARIABLE TEMPORAL) ---
                    BigDecimal cantidadOriginal = detalle.getCantidad();
                    BigDecimal cantidadParaCalculo;

                    // Solo convertimos si es PESO, si no, se queda igual (UNIDAD/LIQUIDO)
                    if ("KGM".equals(productoCompleto.getTipoVenta().getCode())) {
                        cantidadParaCalculo = cantidadOriginal.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
                    } else {
                        cantidadParaCalculo = cantidadOriginal;
                    }

                    // ✅ VALIDACIÓN 1: Cantidad mínima
                    if (productoCompleto.getCantidadMinima() != null &&
                            cantidadParaCalculo.compareTo(productoCompleto.getCantidadMinima()) < 0) {
                        redirectAttributes.addFlashAttribute("info", String.format("La cantidad mínima para '%s' es %s",
                                productoCompleto.getNombre(), productoCompleto.getCantidadMinima()));
                        model.addAttribute("productos", productoService.listarProductos());
                        model.addAttribute("clientes", clienteService.listarcliente());
                        model.addAttribute("pedido", pedido);
                        return "viewPedidos/crearPedidos";
                    }

                    // ✅ VALIDACIÓN 2: Stock suficiente
                    if (productoCompleto.getCantidad().compareTo(cantidadParaCalculo) < 0) {
                        redirectAttributes.addFlashAttribute("info", String.format("Stock insuficiente para '%s'. Disponible: %s",
                                productoCompleto.getNombre(), productoCompleto.getCantidad()));
                        model.addAttribute("productos", productoService.listarProductos());
                        model.addAttribute("clientes", clienteService.listarcliente());
                        model.addAttribute("pedido", pedido);
                        return "redirect:/pedidos/nuevo";
                    }
                    BigDecimal precioAprocesar=BigDecimal.ZERO;
                    if(Boolean.TRUE.equals(pedido.getVentaPorMayor()) && productoCompleto.getPrecioPorMayor() != null){
                        precioAprocesar=productoCompleto.getPrecioPorMayor();
                        pedido.setVentaPorMayor(true);
                    }else{
                        precioAprocesar=productoCompleto.getPrecio();
                    }
                    // Asignar producto y precio real de la BD
                    detalle.setProducto(productoCompleto);
                    detalle.setPedido(pedido);
                    detalle.setPrecioUnitario(precioAprocesar);

                    // Impuestos
                    BigDecimal tasaImpuesto = (productoCompleto.getImpuesto() != null)
                            ? productoCompleto.getImpuesto()
                            : BigDecimal.ZERO;
                    detalle.setPorcentajeImpuesto(tasaImpuesto);

                    // ✅ CALCULO FINANCIERO (Usando la variable temporal cantidadParaCalculo)
                    BigDecimal subtotalItem = detalle.getPrecioUnitario()
                            .multiply(cantidadParaCalculo)
                            .setScale(2, RoundingMode.HALF_UP);
                    detalle.setSubtotal(subtotalItem);

                    BigDecimal impuestoItem = subtotalItem
                            .multiply(tasaImpuesto)
                            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                    // ✅ Actualizar la cantidad en el detalle DESPUÉS de todos los cálculos
                    detalle.setCantidad(cantidadParaCalculo);

                    subtotalPedido = subtotalPedido.add(subtotalItem);
                    montoTotalImpuestos = montoTotalImpuestos.add(impuestoItem);
                    detallesValidos.add(detalle);
                }
            }

            if (pedido.getFlete() == null) {
                pedido.setFlete(BigDecimal.ZERO);
            }

            if (detallesValidos.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debes agregar al menos un producto válido");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);
                return "redirect:/pedidos/nuevo";
            }

            pedido.setDetalles(detallesValidos);
            pedido.setSubtotal(subtotalPedido.setScale(2, RoundingMode.HALF_UP));

            //logica de Descuento para los Pedidos
            BigDecimal PorcentajeDescuento = pedido.getDescuento();
            BigDecimal TotalDescuento = BigDecimal.ZERO;
            if(PorcentajeDescuento == null){
                pedido.setDescuento(BigDecimal.ZERO);
            }else{
                TotalDescuento = PorcentajeDescuento
                        .multiply(subtotalPedido)
                        .divide( new BigDecimal("100"));

                pedido.setDescuento(PorcentajeDescuento);
            }
            pedido.setImpuesto(montoTotalImpuestos.setScale(2, RoundingMode.HALF_UP));

            BigDecimal total = subtotalPedido.add(montoTotalImpuestos)
                    .add(pedido.getFlete())
                    .subtract(TotalDescuento);

            pedido.setTotal(RoundingUtil.roundToColombianPeso(total));

            pedidoService.DescantorStock(pedido);
            pedido.setEstado(EstadoPedido.PENDIENTE);

            pedidoService.guardarpedidos(pedido);

            redirectAttributes.addFlashAttribute("success", "Pedido creado exitosamente");
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el pedido: " + e.getMessage());
            return "redirect:/pedidos/nuevo";
        }
    }
    /**
     * Mostrar formulario para editar pedido
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        try {
            Pedidos pedido = pedidoService.pedidosByid(id);
            if (pedido == null) {
                return "redirect:/pedidos/listarpedidos?error=true";
            }

            // Asegurarse de que el cliente no sea null
            if (pedido.getCliente() == null) {
                pedido.setCliente(new Cliente());
            }

            model.addAttribute("productos", productoService.ProductoSimple());
            model.addAttribute("clientes", clienteService.clienteSimple());

            model.addAttribute("categoria",categoriaService.Categorias());
            model.addAttribute("estadoPedido", EstadoPedido.values());
            model.addAttribute("pedido", pedido);
            return "viewPedidos/actualizarPedido";

        } catch (Exception e) {
            System.err.println("Error al obtener pedido: " + e.getMessage());
            return "redirect:/pedidos/listarpedidos";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarPedido(@PathVariable Long id,
                                   @ModelAttribute("pedido") Pedidos pedido,
                                   RedirectAttributes redirectAttributes, Model model) {
        try {
            // 1. Cargar el pedido real de la base de datos
            Pedidos pedidoExistente = pedidoService.pedidosByid(id);
            if (pedidoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                return "redirect:/pedidos/listarpedidos";
            }

            // IMPORTANTE: sincronizar el flag desde el formulario
            boolean ventaPorMayor = Boolean.TRUE.equals(pedido.getVentaPorMayor());
            pedidoExistente.setVentaPorMayor(ventaPorMayor);

            BigDecimal subtotalProductos = BigDecimal.ZERO;
            BigDecimal montoTotalImpuestos = BigDecimal.ZERO;
            List<DetallePedido> detallesNuevos = new ArrayList<>();

            // 2. Procesar los detalles que vienen del formulario
            if (pedido.getDetalles() != null) {
                for (DetallePedido detalleForm : pedido.getDetalles()) {
                    if (detalleForm.getProducto() != null && detalleForm.getProducto().getId() != null &&
                            detalleForm.getCantidad() != null && detalleForm.getCantidad().compareTo(BigDecimal.ZERO) > 0) {

                        Productos prod = productoService.productoById(detalleForm.getProducto().getId());
                        if (prod != null) {
                            detalleForm.setProducto(prod);
                            detalleForm.setPedido(pedidoExistente);

                            // Elegir precio correcto según ventaPorMayor
                            BigDecimal precioUnit;
                            if (ventaPorMayor && prod.getPrecioPorMayor() != null
                                    && prod.getPrecioPorMayor().compareTo(BigDecimal.ZERO) > 0) {
                                precioUnit = prod.getPrecioPorMayor();
                            } else {
                                precioUnit = prod.getPrecio();
                            }

                            BigDecimal tasaImpuesto = (prod.getImpuesto() != null) ? prod.getImpuesto() : BigDecimal.ZERO;

                            detalleForm.setPrecioUnitario(precioUnit);
                            detalleForm.setPorcentajeImpuesto(tasaImpuesto);

                            // --- TRANSFORMACIÓN SEGÚN TIPO DE VENTA ---
                            BigDecimal cantidadOriginal = detalleForm.getCantidad();
                            BigDecimal cantTransformada;

                            if ("KGM".equals(prod.getTipoVenta().getCode())) {
                                cantTransformada = cantidadOriginal.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
                            } else {
                                cantTransformada = cantidadOriginal;
                            }

                            detalleForm.setCantidad(cantTransformada);

                            // --- CÁLCULO DE TOTALES POR LÍNEA ---
                            BigDecimal subtotalItem = precioUnit.multiply(cantTransformada);
                            BigDecimal impuestoItem = subtotalItem.multiply(tasaImpuesto)
                                    .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

                            detalleForm.setSubtotal(subtotalItem.setScale(2, RoundingMode.HALF_UP));

                            subtotalProductos = subtotalProductos.add(detalleForm.getSubtotal());
                            montoTotalImpuestos = montoTotalImpuestos.add(impuestoItem);

                            detallesNuevos.add(detalleForm);
                        }
                    }
                }
            }

            if (pedido.getFlete() == null) {
                pedido.setFlete(BigDecimal.ZERO);
            }

            if (detallesNuevos.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Debes agregar al menos un producto válido");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);
                return "redirect:/pedidos/listar";
            }

            BigDecimal PorcentajeDescuento = pedido.getDescuento();
            BigDecimal TotalDescuento = BigDecimal.ZERO;
            if(PorcentajeDescuento == null){
                pedido.setDescuento(BigDecimal.ZERO);
            }else{
                TotalDescuento = PorcentajeDescuento
                        .multiply(subtotalProductos)
                        .divide( new BigDecimal("100"));

                pedido.setDescuento(PorcentajeDescuento);
            }
            // 3. Lógica Financiera Final
            BigDecimal flete = (pedido.getFlete() != null) ? pedido.getFlete() : BigDecimal.ZERO;
            BigDecimal totalFinal = subtotalProductos.add(flete).add(montoTotalImpuestos).subtract(TotalDescuento);

            // 4. Sincronizar el objeto existente
            pedidoExistente.setFechaEntrega(pedido.getFechaEntrega());
            pedidoExistente.setFlete(flete);
            pedidoExistente.setSubtotal(subtotalProductos.setScale(2, RoundingMode.HALF_UP));
            pedidoExistente.setImpuesto(montoTotalImpuestos.setScale(2, RoundingMode.HALF_UP));
            pedidoExistente.setTotal(RoundingUtil.roundToColombianPeso(totalFinal));
            pedidoExistente.actualizarDetalles(detallesNuevos);

            // 5. Guardar cambios
            pedidoService.Updatepedido(id, pedidoExistente);

            redirectAttributes.addFlashAttribute("success", "Pedido actualizado correctamente");
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error crítico al actualizar: " + e.getMessage());
            return "redirect:/pedidos/listarpedidos";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            redirectAttributes.addFlashAttribute("success", "Pedido eliminado correctamente");
            pedidoService.deletepedidos(id);
            System.out.println("Pedido eliminado con ID: " + id);
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar pedido: " + e.getMessage());
            System.err.println("Error al eliminar pedido: " + e.getMessage());
            return "redirect:/pedidos/listarpedidos";
        }
    }

    @GetMapping("/Entregar/{id}")
    public String entregarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Obtener el pedido existente
            Pedidos pedidoExistente = pedidoService.pedidosByid(id);

            if (pedidoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                return "redirect:/pedidos/listarpedidos";
            }

            // Verificar que el pedido no esté ya entregado o cancelado
            if (pedidoExistente.getEstado() == EstadoPedido.ENTREGADO) {
                redirectAttributes.addFlashAttribute("info", "El pedido ya está entregado");
                return "redirect:/pedidos/listarpedidos";
            }

            if (pedidoExistente.getEstado() == EstadoPedido.CANCELADO) {
                redirectAttributes.addFlashAttribute("error", "No se puede entregar un pedido cancelado");
                return "redirect:/pedidos/listarpedidos";
            }

            pedidoService.EntregarPedido(id);

            System.out.println("Pedido " + id + " marcado como ENTREGADO");
            redirectAttributes.addFlashAttribute("success",
                    "Pedido #" + id + " entregado exitosamente");
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            System.err.println("Error al entregar pedido: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al entregar el pedido: " + e.getMessage());
            return "redirect:/pedidos/listarpedidos";
        }
    }


    @GetMapping("/Cancelar/{id}")
    public String cancelarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Obtener el pedido existente
            Pedidos pedidoExistente = pedidoService.pedidosByid(id);

            if (pedidoExistente == null) {
                redirectAttributes.addFlashAttribute("info", "Pedido no encontrado");
                return "redirect:/pedidos/listarpedidos";
            }

            // Verificar que el pedido no esté ya cancelado
            if (pedidoExistente.getEstado() == EstadoPedido.CANCELADO) {
                redirectAttributes.addFlashAttribute("success", "El pedido ya está cancelado");
                return "redirect:/pedidos/listarpedidos";
            }

            // Verificar que el pedido no esté entregado
            if (pedidoExistente.getEstado() == EstadoPedido.ENTREGADO) {
                redirectAttributes.addFlashAttribute("error",
                        "No se puede cancelar un pedido que ya fue entregado");
                return "redirect:/pedidos/listarpedidos";
            }

            pedidoService.RestaurarStock(pedidoExistente);
            // Cambiar el estado a CANCELADO
            pedidoExistente.setEstado(EstadoPedido.CANCELADO);

            // Actualizar el pedido
            pedidoService.CancelarPedido(id);

            System.out.println("Pedido " + id + " cancelado exitosamente");

            redirectAttributes.addFlashAttribute("success",
                    "Pedido #" + id + " cancelado exitosamente");
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            System.err.println("Error al cancelar pedido: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error",
                    "Error al cancelar el pedido: " + e.getMessage());
            return "redirect:/pedidos/listarpedidos";
        }
    }

    @GetMapping("/generarTicket/{id}")
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) throws Exception {

        Pedidos pedido = pedidoService.pedidosByid(id);
        Empresa empresa = securityService.ObtenerEmpresa();

        BigDecimal subtotal = pedido.getSubtotal();
        BigDecimal impuesto = pedido.getImpuesto();
        BigDecimal total = pedido.getTotal();
        BigDecimal flete = (pedido.getFlete() != null) ? pedido.getFlete() : BigDecimal.ZERO;

        // Calculamos el valor real del descuento en moneda
        BigDecimal porcentajeDescuento = (pedido.getDescuento() != null) ? pedido.getDescuento() : BigDecimal.ZERO;
        BigDecimal valorDescuento = subtotal.multiply(porcentajeDescuento).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        Map<String, Object> model = new HashMap<>();
        model.put("pedido", pedido);
        model.put("subtotal", subtotal);
        model.put("impuesto", impuesto);
        model.put("flete", flete);
        model.put("total", total);
        model.put("valorDescuento", valorDescuento); // Valor en $
        model.put("porcentajeDescuento", porcentajeDescuento); // Valor en %
        model.put("empresa", empresa);
        model.put("usuario", securityService.obtenerEmailUsuario()); // O usar user.getUsername()

        byte[] pdf = pdfService.generarPdf("pdf/ticketPedidos", model);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=ticket_pedido_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}