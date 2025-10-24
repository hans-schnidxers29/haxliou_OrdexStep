package com.example.demo.controlador;


import com.example.demo.entidad.DetallePedido;
import com.example.demo.entidad.Pedidos;
import com.example.demo.servicio.ClienteService;
import com.example.demo.servicio.PedidoService;
import com.example.demo.servicio.ProductoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

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
    @GetMapping
    public String listarPedidos(Model model) {
        model.addAttribute("pedidos", pedidoService.listarpedidos());
        return "viewPedidos/index";
    }

    /**
     * Listar pedidos - ruta alternativa
     */
    @GetMapping("/listarpedidos")
    public String listarPedidosAlternativo(Model model) {
        return listarPedidos(model);
    }

    /**
     * Alias para listar pedidos
     */
    @GetMapping("/listar")
    public String listarPedidosAlias(Model model) {
        return listarPedidos(model);
    }

    /**
     * Mostrar formulario para crear nuevo pedido
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("productos", productoService.listarProductos());
        model.addAttribute("clientes", clienteService.listarcliente());

        Pedidos pedido = new Pedidos();
        DetallePedido detalle = new DetallePedido();
        pedido.getDetalles().add(detalle);

        model.addAttribute("pedidos", pedido);
        return "viewPedidos/crearPedidos";
    }

    /**
     * Guardar nuevo pedido
     */
    @PostMapping("/crear")
    public String guardarPedido(@ModelAttribute("pedidos") Pedidos pedido,
                                RedirectAttributes redirectAttributes,
                                Model model) {
        try {
            // Validar que tenga detalles
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                model.addAttribute("error", "El pedido debe tener al menos un producto");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedidos", pedido);
                return "viewPedidos/crearPedidos";
            }

            // Validar cliente
            if (pedido.getCliente() == null ) {
                model.addAttribute("error", "Debes seleccionar un cliente");
                model.addAttribute("productos", productoService.listarProductos());
                model.addAttribute("clientes", clienteService.listarcliente());
                model.addAttribute("pedidos", pedido);
                return "viewPedidos/crearPedidos";
            }

            // Asociar el pedido a cada detalle y calcular totales
            BigDecimal subtotalPedido = BigDecimal.ZERO;
            for (DetallePedido detalle : pedido.getDetalles()) {
                // Solo procesar detalles con producto válido
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
                model.addAttribute("pedidos", pedido);
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

            redirectAttributes.addAttribute("success", "true");
            return "redirect:/pedidos?success=true";

        } catch (Exception e) {
            System.err.println("Error al guardar pedido: " + e.getMessage());
            e.printStackTrace();

            redirectAttributes.addAttribute("error", "true");
            return "redirect:/pedidos?error=true";
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
                return "redirect:/pedidos?error=true";
            }

            model.addAttribute("productos", productoService.listarProductos());
            model.addAttribute("clientes", clienteService.listarcliente());
            model.addAttribute("pedidos", pedido);
            return "viewPedidos/crearPedidos";

        } catch (Exception e) {
            System.err.println("Error al obtener pedido: " + e.getMessage());
            return "redirect:/pedidos?error=true";
        }
    }

    /**
     * Ver detalles del pedido
     */
    @GetMapping("/ver/{id}")
    public String verDetalles(@PathVariable Long id, Model model) {
        try {
            Pedidos pedido = pedidoService.pedidosByid(id);
            if (pedido == null) {
                return "redirect:/pedidos?error=true";
            }

            model.addAttribute("pedido", pedido);
            return "viewPedidos/verPedido";

        } catch (Exception e) {
            System.err.println("Error al obtener pedido: " + e.getMessage());
            return "redirect:/pedidos?error=true";
        }
    }

    /**
     * Eliminar pedido
     */
    @GetMapping("/eliminar/{id}")
    public String eliminarPedido(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            pedidoService.deletepedidos(id);
            System.out.println("Pedido eliminado con ID: " + id);
            redirectAttributes.addAttribute("success", "true");
            return "redirect:/pedidos?success=true";

        } catch (Exception e) {
            System.err.println("Error al eliminar pedido: " + e.getMessage());
            redirectAttributes.addAttribute("error", "true");
            return "redirect:/pedidos?error=true";
        }
    }

    /**
     * Endpoint alternativo para crear pedidos (compatibilidad con rutas antiguas)
     */
    @PostMapping("/crear/pedidos")
    public String guardarPedidoAlternativo(@ModelAttribute("pedidos") Pedidos pedido,
                                           RedirectAttributes redirectAttributes,
                                           Model model) {
        return guardarPedido(pedido, redirectAttributes, model);
    }
}