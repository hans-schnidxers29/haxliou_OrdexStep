package com.example.demo.controlador;


import com.example.demo.entidad.*;
import com.example.demo.servicio.ClienteService;
import com.example.demo.servicio.PedidoService;
import com.example.demo.servicio.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/pedidos")
public class PedidosControlador {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private ProductoServicio productoService;

    @Autowired
    private ClienteService clienteService;

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
        model.addAttribute("clientes", clienteService.listarcliente());

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
            List<DetallePedido> detallesValidos = new ArrayList<>();

            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.getProducto() != null &&
                        detalle.getProducto().getId() != null &&
                        detalle.getCantidad() != null &&
                        detalle.getCantidad() > 0) {

                    // Buscar producto completo desde la BD
                    Productos productoCompleto = productoService.productoById(detalle.getProducto().getId());

                    if (productoCompleto == null) {
                        model.addAttribute("info", "Producto no encontrado");
                        model.addAttribute("productos", productoService.listarProductos());
                        model.addAttribute("clientes", clienteService.listarcliente());
                        model.addAttribute("pedido", pedido);
                        return "viewPedidos/crearPedidos";
                    }

                    // Verificar stock disponible
                    if (productoCompleto.getCantidad() < detalle.getCantidad()) {
                        model.addAttribute("info", "Stock insuficiente para: " + productoCompleto.getNombre() +
                                ". Disponible: " + productoCompleto.getCantidad());
                        model.addAttribute("productos", productoService.listarProductos());
                        model.addAttribute("clientes", clienteService.listarcliente());
                        model.addAttribute("pedido", pedido);
                        return "viewPedidos/crearPedidos";
                    }

                    // Asignar producto completo
                    detalle.setProducto(productoCompleto);
                    detalle.setPedido(pedido);

                    // Establecer precio unitario si no viene del formulario
                    if (detalle.getPrecioUnitario() == null) {
                        detalle.setPrecioUnitario(productoCompleto.getPrecio());
                    }

                    // Calcular subtotal del detalle
                    BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
                    detalle.setSubtotal(detalle.getPrecioUnitario().multiply(cantidad));

                    subtotalPedido = subtotalPedido.add(detalle.getSubtotal());
                    detallesValidos.add(detalle);
                }
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
            if (pedido.getImpuesto() == null) {
                pedido.setImpuesto(BigDecimal.ZERO);
            }
            BigDecimal total = subtotalPedido.add(pedido.getImpuesto());
            pedido.setTotal(total);

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
            // Validar que el pedido exista
            Pedidos pedidoExistente = pedidoService.pedidosByid(id);
            if (pedidoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                return "redirect:/pedidos/listarpedidos?error=true";
            }
            EstadoPedido estadoante = pedidoExistente.getEstado();
            BigDecimal subtotalPedido = BigDecimal.ZERO;
            List<DetallePedido> detallesValidos = new ArrayList<>();

            if (pedido.getDetalles() != null) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    if (detalle.getProducto() != null &&
                            detalle.getProducto().getId() != null &&
                            detalle.getCantidad() != null &&
                            detalle.getCantidad() > 0) {

                        // Buscar producto completo desde la BD
                        Productos productoCompleto = productoService.productoById(detalle.getProducto().getId());
                        if (productoCompleto == null) {
                            redirectAttributes.addFlashAttribute("info",
                                    "Producto no encontrado con ID: " + detalle.getProducto().getId());
                            return "redirect:/pedidos/listarpedidos";
                        }

                        // Asociar producto y pedido correctos
                        detalle.setProducto(productoCompleto);
                        detalle.setPedido(pedidoExistente);

                        // Calcular subtotal si no está definido
                        if (detalle.getPrecioUnitario() == null) {
                            detalle.setPrecioUnitario(productoCompleto.getPrecio());
                        }

                        BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
                        detalle.setSubtotal(detalle.getPrecioUnitario().multiply(cantidad));

                        subtotalPedido = subtotalPedido.add(detalle.getSubtotal());
                        detallesValidos.add(detalle);
                    }
                }
            }

            // Reemplazar los detalles válidos
            pedido.setDetalles(detallesValidos);

            // Calcular totales
            pedido.setSubtotal(subtotalPedido);
            if (pedido.getImpuesto() == null) pedido.setImpuesto(BigDecimal.ZERO);
            pedido.setTotal(subtotalPedido.add(pedido.getImpuesto()));

            if(pedido.getEstado()==EstadoPedido.ENTREGADO && estadoante!=EstadoPedido.ENTREGADO){
                System.out.println("Estado cambió a ENTREGADO - Descontando stock...");
                try {
                    pedidoService.DescantorStock(pedido);
                    System.out.println("Stock descontado exitosamente");
                } catch (Exception e) {
                    System.err.println("Error al descontar stock: " + e.getMessage());
                    redirectAttributes.addFlashAttribute("error",
                            "Error al descontar stock: " + e.getMessage());
                    return "redirect:/pedidos/editar/" + id;
                }
            }
            // Actualizar pedido usando el servicio
            pedidoService.Updatepedido(id, pedido);

            redirectAttributes.addFlashAttribute("success", "Pedido actualizado correctamente");
            return "redirect:/pedidos/listarpedidos";

        } catch (Exception e) {
            System.err.println("Error al actualizar pedido: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el pedido: " + e.getMessage());
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

            // Cambiar el estado a CANCELADO
            pedidoExistente.setEstado(EstadoPedido.CANCELADO);

            // Actualizar el pedido
            pedidoService.Updatepedido(id, pedidoExistente);

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

}