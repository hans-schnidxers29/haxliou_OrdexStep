package com.example.demo.Login.Controlador;


import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.ModuloVentas.VentaServicio;
import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.servicio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControldorInicio {

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio ventaServicio;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PedidoService pedidoservicio;

    @Autowired
    private EgresoServicio egresoServicio;

    @Autowired
    private CompraServicio compraServicio;

    @Autowired private ProveedorServicio proveedorServicio;





    @GetMapping("/login")
    public String iniciarSesion() {
        return "Login/login";
    }

    @GetMapping( "/Home")
    public String verPaginaDeInicio(Model modelo) {
        modelo.addAttribute("usuarios", servicioUsuario.ListarUSer());
        modelo.addAttribute("recaudacionMes", ventaServicio.TotalVentasMesActual());
        modelo.addAttribute("totalClientes", clienteService.listarcliente().size());
        modelo.addAttribute("totalPedidosPendientes", pedidoservicio.listarpedidos()
                .stream().map(p -> p.getEstado()).filter(estado -> estado.equals(EstadoPedido.PENDIENTE))
                .toList()
                .size());
        modelo.addAttribute("metodosPagoLabels", ventaServicio.ListaMetodosPago());
        modelo.addAttribute("metodosPagoValores", ventaServicio.ListaMetodosPagoValores());
        modelo.addAttribute("productosAlerta", productoServicio.verificarStock());
        modelo.addAttribute("totalProductosBajoStock", productoServicio.verificarStock().stream()
                .map(bajo -> (Number) bajo[1]).toList().size());
        return "Home/Home";
    }

    @GetMapping("/reportes")
    public String MostrarformReportes(Model model) {
        model.addAttribute("ventas",ventaServicio.ListarVenta());
        model.addAttribute("pedidos",pedidoservicio.listarpedidos());
        model.addAttribute("clientes",clienteService.listarcliente());
        model.addAttribute("egresos",egresoServicio.ListarGastos());
        model.addAttribute("totalPedidos",pedidoservicio.listarpedidos().size());
        model.addAttribute("recaudacionTotal",ventaServicio.TotalVentasMesActual());
        model.addAttribute("compras",compraServicio.listarCompra());
        model.addAttribute("productos",productoServicio.listarProductos());
        model.addAttribute("proveedores",proveedorServicio.listarproveedores());
        return "pdf/reportes";
    }

}
