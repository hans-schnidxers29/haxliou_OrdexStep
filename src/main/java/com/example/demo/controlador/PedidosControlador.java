package com.example.demo.controlador;


import com.example.demo.Login.Servicio.ServicioEmpresa;
import com.example.demo.entidad.*;
import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.pdf.PdfServicio;
import com.example.demo.servicio.CategoriaService;
import com.example.demo.servicio.ClienteService;
import com.example.demo.servicio.PedidoService;
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

    /**
     * Listar todos los pedidos
     */
    @GetMapping("/listarpedidos")  // ← AGREGAR la barra
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarpedidos());
        model.addAttribute("Estadisticas",pedidoService.ContarPorestados(EstadoPedido.PENDIENTE));
        model.addAttribute("Estadisticas2",pedidoService.estadoCEntregado(EstadoPedido.ENTREGADO));
        model.addAttribute("Estadisticas3",pedidoService.estadoCancelado(EstadoPedido.CANCELADO));
        model.addAttribute("conteoPedidos",clienteService.ListaCLientePedidos());
        model.addAttribute("nombresClientes",clienteService.NombreListPedidos());
        return "viewPedidos/index";
    }

    /**
     * Mostrar formulario para crear nuevo pedido
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("clientes", clienteService.clienteSimple());
        model.addAttribute("categoria",categoriaService.Listarcategoria());

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
            // Validar que tenga detalles
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                model.addAttribute("info", "El pedido debe tener al menos un producto");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);
                return "viewPedidos/crearPedidos";
            }

            // Validar cliente
            if (pedido.getCliente() == null || pedido.getCliente().getId() == null) {
                model.addAttribute("info", "Debes seleccionar un cliente");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);
                return "viewPedidos/crearPedidos";
            }

            // ✅ BUSCAR CLIENTE COMPLETO DESDE LA BD
            Cliente clienteCompleto = clienteService.clientdById(pedido.getCliente().getId());
            if (clienteCompleto == null) {
                model.addAttribute("info", "Cliente no encontrado");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);
                return "viewPedidos/crearPedidos";
            }
            pedido.setCliente(clienteCompleto);

            // ✅ BUSCAR PRODUCTOS COMPLETOS Y CALCULAR TOTALES
            BigDecimal subtotalPedido = BigDecimal.ZERO;
            BigDecimal montoTotalImpuestos = BigDecimal.ZERO; // Nueva variable acumuladora
            List<DetallePedido> detallesValidos = new ArrayList<>();

            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.getProducto() != null &&
                        detalle.getProducto().getId() != null &&
                        detalle.getCantidad() != null &&
                        detalle.getCantidad().compareTo(BigDecimal.ZERO) > 0) {

                    // Buscar producto completo desde la BD
                    Productos productoCompleto = productoService.productoById(detalle.getProducto().getId());

                    if (productoCompleto == null) {
                        model.addAttribute("info", "Producto no encontrado");
                        model.addAttribute("productos", productoService.listarProductos());
                        model.addAttribute("clientes", clienteService.listarcliente());
                        model.addAttribute("pedido", pedido);
                        return "viewPedidos/crearPedidos";
                    }

                    // --- LÓGICA DE CONVERSIÓN GRAMOS A KILOS ---
                    BigDecimal cantidadOriginal = detalle.getCantidad(); // Lo que viene del form (ej: 500g)
                    BigDecimal cantidadProcesada = cantidadOriginal;

                    // Si el producto se vende por PESO, convertimos los gramos recibidos a KG
                    if ("PESO".equals(productoCompleto.getTipoVenta().name())) {
                        cantidadProcesada = cantidadOriginal.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
                        // Seteamos la cantidad convertida al detalle para que el stock se descuente correctamente
                        detalle.setCantidad(cantidadProcesada);
                    }

                    // ✅ VALIDACIÓN 1: Cantidad mínima de venta (comparando en la unidad base: KG)
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

                    // Asignar producto completo
                    detalle.setProducto(productoCompleto);
                    detalle.setPedido(pedido);

                    // Establecer precio unitario si no viene del formulario
                    if (detalle.getPrecioUnitario() == null) {
                        detalle.setPrecioUnitario(productoCompleto.getPrecio());
                    }

                    // --- NUEVA LOGICA IMPUESTO INDIVIDUAL ---
                    BigDecimal tasaImpuesto = (productoCompleto.getImpuesto() != null) ? productoCompleto.getImpuesto() : BigDecimal.ZERO;
                    detalle.setPorcentajeImpuesto(tasaImpuesto);

                    // Calcular subtotal del detalle
                    BigDecimal cantidad = detalle.getCantidad();
                    BigDecimal subtotalItem = detalle.getPrecioUnitario().multiply(cantidad);
                    detalle.setSubtotal(subtotalItem.setScale(3, RoundingMode.HALF_UP));

                    // Calcular monto de impuesto de este item: (subtotal * tasa) / 100
                    BigDecimal impuestoItem = subtotalItem.multiply(tasaImpuesto).divide(new BigDecimal("100"), 3, RoundingMode.HALF_UP);

                    subtotalPedido = subtotalPedido.add(subtotalItem);
                    montoTotalImpuestos = montoTotalImpuestos.add(impuestoItem);
                    detallesValidos.add(detalle);
                }
            }

            if (pedido.getFlete() == null){
                pedido.setFlete(BigDecimal.ZERO);
            }

            // Validar que haya al menos un detalle válido
            if (detallesValidos.isEmpty()) {
                model.addAttribute("error", "Debes agregar al menos un producto válido");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);
                return "viewPedidos/crearPedidos";
            }

            // Establecer los detalles válidos
            pedido.setDetalles(detallesValidos);
            // Calcular total del pedido
            pedido.setSubtotal(subtotalPedido);

            // ✅ ASIGNAR MONTO TOTAL DE IMPUESTOS CALCULADO
            pedido.setImpuesto(montoTotalImpuestos);

            BigDecimal total = subtotalPedido.add(montoTotalImpuestos).add(pedido.getFlete());
            pedido.setTotal(total.setScale(3, RoundingMode.HALF_UP));

            pedidoService.DescantorStock(pedido);
            pedido.setEstado(EstadoPedido.PENDIENTE);


            // Guardar el pedido
            Pedidos pedidoGuardado = pedidoService.guardarpedidos(pedido);
            System.out.println("Pedido guardado con ID: " + pedidoGuardado.getId() +
                    " - Cliente: " + pedidoGuardado.getCliente().getNombre());

            redirectAttributes.addFlashAttribute("success", "Pedido creado exitosamente");
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            System.err.println("Error al guardar pedido: " + e.getMessage());
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

            model.addAttribute("productos", productoService.listarProductos());
            model.addAttribute("clientes", clienteService.listarcliente());
            model.addAttribute("categoria",categoriaService.Listarcategoria());
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
                                   RedirectAttributes redirectAttributes) {
        try {
            Pedidos pedidoExistente = pedidoService.pedidosByid(id);
            if (pedidoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                return "redirect:/pedidos/listarpedidos";
            }

            EstadoPedido estadoAnterior = pedidoExistente.getEstado();
            BigDecimal subtotalProductos = BigDecimal.ZERO;
            BigDecimal montoTotalImpuestos = BigDecimal.ZERO; // Nueva variable acumuladora
            List<DetallePedido> detallesValidos = new ArrayList<>();

            if (pedido.getDetalles() != null) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    if (detalle.getProducto() != null && detalle.getProducto().getId() != null &&
                            detalle.getCantidad() != null && detalle.getCantidad().compareTo(BigDecimal.ZERO) > 0) {

                        Productos prod = productoService.productoById(detalle.getProducto().getId());
                        if (prod != null) {
                            detalle.setProducto(prod);
                            detalle.setPedido(pedidoExistente);

                            if (detalle.getPrecioUnitario() == null) {
                                detalle.setPrecioUnitario(prod.getPrecio());
                            }

                            // --- CORRECCIÓN AQUÍ ---
                            BigDecimal cantidadOriginal = detalle.getCantidad();
                            BigDecimal cantTransformada;

                            if ("PESO".equals(prod.getTipoVenta().name())) {
                                // Convertimos gramos a kilos
                                cantTransformada = cantidadOriginal.divide(new BigDecimal("1000"), 3, RoundingMode.HALF_UP);
                                // IMPORTANTE: Seteamos la nueva cantidad en kilos al detalle para que se guarde así en la BD
                                detalle.setCantidad(cantTransformada);
                            } else {
                                cantTransformada = cantidadOriginal;
                            }

                            // --- NUEVA LOGICA IMPUESTO INDIVIDUAL ---
                            BigDecimal tasaImpuesto = (prod.getImpuesto() != null) ? prod.getImpuesto() : BigDecimal.ZERO;
                            detalle.setPorcentajeImpuesto(tasaImpuesto);

                            BigDecimal subtotalItem = detalle.getPrecioUnitario().multiply(cantTransformada);
                            detalle.setSubtotal(subtotalItem.setScale(3, RoundingMode.HALF_UP));

                            // Calcular monto de impuesto de este item
                            BigDecimal impuestoItem = subtotalItem.multiply(tasaImpuesto).divide(new BigDecimal("100"), 3, RoundingMode.HALF_UP);

                            subtotalProductos = subtotalProductos.add(detalle.getSubtotal());
                            montoTotalImpuestos = montoTotalImpuestos.add(impuestoItem);
                            detallesValidos.add(detalle);
                        }
                    }
                }
            }

            // 2. Lógica Financiera
            BigDecimal flete = (pedido.getFlete() != null) ? pedido.getFlete() : BigDecimal.ZERO;

            // ✅ ELIMINAMOS EL CALCULO BASADO EN PorcentajeImpuesto GLOBAL Y USAMOS EL ACUMULADO
            BigDecimal totalFinal = subtotalProductos.add(flete).add(montoTotalImpuestos);

            // 3. Asignar valores
            pedido.setDetalles(detallesValidos);
            pedido.setSubtotal(subtotalProductos);
            pedido.setFlete(flete);
            pedido.setImpuesto(montoTotalImpuestos); // Asignamos el acumulado de cada producto
            pedido.setTotal(totalFinal.setScale(3, RoundingMode.HALF_UP));

            pedidoService.Updatepedido(id, pedido);

            redirectAttributes.addFlashAttribute("success", "Pedido actualizado correctamente");
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
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
    public ResponseEntity<byte[]> generarTicket(@PathVariable Long id) throws Exception{

        Pedidos pedido = pedidoService.pedidosByid(id);

        Empresa empresa = empresaService.DatosEmpresa(1L);

        BigDecimal Subtotal = pedido.getSubtotal();
        BigDecimal Impuesto = pedido.getImpuesto(); // Ahora simplemente usamos el campo impuesto del pedido
        BigDecimal Total = pedido.getTotal();
        BigDecimal flete = (pedido.getFlete() != null) ? pedido.getFlete() : BigDecimal.ZERO;

        Map<String, Object> model = new HashMap<>();
        model.put("pedido", pedido);
        model.put("subtotal", Subtotal);
        model.put("impuesto", Impuesto);
        model.put("flete",flete);
        model.put("total", Total);
        model.put("empresa",empresa);
        byte[] pdf = pdfService.generarPdf("pdf/ticketPedidos", model);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ticket_pedido_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}