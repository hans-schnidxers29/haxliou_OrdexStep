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
    public String guardarPedido(@ModelAttribute("pedido") Pedidos pedido,  // ← CAMBIAR a "pedido"
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            // Validar que tenga detalles
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                model.addAttribute("error", "El pedido debe tener al menos un producto");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);  // ← CAMBIAR a "pedido"
                return "viewPedidos/crearPedidos";
            }

            // Validar cliente
            if (pedido.getCliente() == null || pedido.getCliente().getId()== null) {  // ← Agregar validación de ID
                model.addAttribute("error", "Debes seleccionar un cliente");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);  // ← CAMBIAR a "pedido"
                return "viewPedidos/crearPedidos";
            }

            // Asociar el pedido a cada detalle y calcular totales
            BigDecimal subtotalPedido = BigDecimal.ZERO;
            for (DetallePedido detalle : pedido.getDetalles()) {
                if (detalle.getProducto() != null && detalle.getProducto().getId() != null) {
                    detalle.setPedido(pedido);
                    if (detalle.getSubtotal() == null) {
                        detalle.setSubtotal(BigDecimal.ZERO);
                    }
                    subtotalPedido = subtotalPedido.add(detalle.getSubtotal());
                }
            }

            // Filtrar detalles válidos
            pedido.setDetalles(pedido.getDetalles().stream()
                    .filter(d -> d.getProducto() != null && d.getProducto().getId() != null)
                    .toList());

            if (pedido.getDetalles().isEmpty()) {
                model.addAttribute("error", "Debes agregar al menos un producto válido");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedido", pedido);  // ← CAMBIAR a "pedido"
                return "viewPedidos/crearPedidos";
            }

            // Calcular total del pedido
            pedido.setSubtotal(subtotalPedido);
            if (pedido.getImpuesto() == null) {
                pedido.setImpuesto(BigDecimal.ZERO);
            }
            BigDecimal total = subtotalPedido.add(pedido.getImpuesto());
            pedido.setTotal(total);

            // Guardar el pedido
            Pedidos pedidoGuardado = pedidoService.guardarpedidos(pedido);
            System.out.println("Pedido guardado con ID: " + pedidoGuardado.getId() +
                    " - Cliente: " + pedidoGuardado.getCliente().getNombre());




            return "redirect:/pedidos/listarpedidos?success=true";  // ← CAMBIAR redirect

        } catch (Exception e) {
            System.err.println("Error al guardar pedido: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al guardar el pedido");
            return "redirect:/pedidos/listarpedidos?error=true";  // ← CAMBIAR redirect
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
            return "redirect:/pedidos/listarpedidos?error=true";
        }
    }

    @PostMapping("/actualizar/{id}")
    public String ActualizarPedido(@PathVariable Long id,
                                   @ModelAttribute("pedido") Pedidos pedido,
                                   RedirectAttributes redirectAttributes) {
        try {
            // Validar que el pedido existe
            Pedidos pedidoExistente = pedidoService.pedidosByid(id);
            if (pedidoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                return "redirect:/pedidos/listarpedidos?error=true";
            }

            // Procesar detalles antes de enviar al servicio
            BigDecimal subtotalPedido = BigDecimal.ZERO;
            List<DetallePedido> detallesValidos = new ArrayList<>();

            if (pedido.getDetalles() != null) {
                for (DetallePedido detalle : pedido.getDetalles()) {
                    // Solo procesar detalles con producto válido
                    if (detalle.getProducto() != null &&
                            detalle.getProducto().getId() != null &&
                            detalle.getCantidad() != null &&
                            detalle.getCantidad() > 0) {

                        // Calcular subtotal del detalle si no está calculado
                        if (detalle.getSubtotal() == null && detalle.getPrecioUnitario() != null) {
                            BigDecimal cantidad = new BigDecimal(detalle.getCantidad());
                            detalle.setSubtotal(detalle.getPrecioUnitario().multiply(cantidad));
                        }

                        subtotalPedido = subtotalPedido.add(detalle.getSubtotal());
                        detallesValidos.add(detalle);
                    }
                }
            }

            // Establecer los detalles válidos
            pedido.setDetalles(detallesValidos);

            // Calcular y establecer totales
            pedido.setSubtotal(subtotalPedido);

            if (pedido.getImpuesto() == null) {
                pedido.setImpuesto(BigDecimal.ZERO);
            }

            BigDecimal total = subtotalPedido.add(pedido.getImpuesto());
            pedido.setTotal(total);

            // El servicio se encarga de todo lo demás
            pedidoService.Updatepedido(id, pedido);

            redirectAttributes.addFlashAttribute("success", "Pedido actualizado correctamente");
            return "redirect:/pedidos/listarpedidos?success=true";

        } catch (Exception e) {
            System.err.println("Error al actualizar pedido: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el pedido: " + e.getMessage());
            return "redirect:/pedidos/listarpedidos?error=true";
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.deletepedidos(id);
            System.out.println("Pedido eliminado con ID: " + id);
            return "redirect:/pedidos/listarpedidos?success=true";

        } catch (Exception e) {
            System.err.println("Error al eliminar pedido: " + e.getMessage());
            return "redirect:/pedidos/listarpedidos?error=true";
        }
    }


}