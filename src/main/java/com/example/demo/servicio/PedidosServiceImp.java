package com.example.demo.servicio;

import com.example.demo.entidad.DetallePedido;
import com.example.demo.entidad.EstadoPedido;
import com.example.demo.entidad.Pedidos;
import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.PedidoRepositorio;
import com.example.demo.repositorio.ProductoRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidosServiceImp implements PedidoService{

    @Autowired
    private PedidoRepositorio repositorio;

    @Autowired
    private ProductoServicio productoServicio;

    @Override
    public List<Pedidos> listarpedidos() {
        return repositorio.findAll();
    }

    @Override
    public Pedidos guardarpedidos(Pedidos pedidos) {
        return repositorio.save(pedidos);
    }

    @Override
    public void deletepedidos(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    public Pedidos pedidosByid(Long id) {
        return repositorio.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void Updatepedido(Long id, Pedidos pedidos) {
        Pedidos pedidos1 = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));

        // Actualizar campos simples
        pedidos1.setCliente(pedidos.getCliente());
        pedidos1.setFechaEntrega(pedidos.getFechaEntrega());
        pedidos1.setFechaPedido(pedidos.getFechaPedido());
        pedidos1.setEstado(pedidos.getEstado());
        pedidos1.setObservaciones(pedidos.getObservaciones());

        // ✅ Actualizar detalles usando el método helper
        pedidos1.actualizarDetalles(pedidos.getDetalles());

        // Actualizar totales
        pedidos1.setSubtotal(pedidos.getSubtotal());
        pedidos1.setImpuesto(pedidos.getImpuesto());
        pedidos1.setTotal(pedidos.getTotal());

        System.out.println("Pedido actualizado correctamente");
        repositorio.save(pedidos1);
    }

    @Override
    public long ContarPorestados(EstadoPedido estadoPedido) {
        return repositorio.contarPorEstado(EstadoPedido.PENDIENTE);
    }

    @Transactional
    @Override
    public void DescantorStock(Pedidos pedidos) {
        for (DetallePedido detalle : pedidos.getDetalles()) {


            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                continue;
            }

            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                continue;
            }

            // Buscar el producto completo desde la BD
            Productos productoBD = productoServicio.productoById(detalle.getProducto().getId());

            // Verificar stock suficiente
            if (productoBD.getCantidad() < detalle.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + productoBD.getNombre() +
                        ". Disponible: " + productoBD.getCantidad() +
                        ", Solicitado: " + detalle.getCantidad());
            }

            // Descontar stock
            int nuevaCantidad = productoBD.getCantidad() - detalle.getCantidad();
            productoBD.setCantidad(nuevaCantidad);

            // Asociar el producto completo al detalle
            detalle.setProducto(productoBD);

            // Guardar producto actualizado
            productoServicio.save(productoBD);

            System.out.println("Stock actualizado - Producto: " + productoBD.getNombre() +
                    " - Cantidad anterior: " + (productoBD.getCantidad() + detalle.getCantidad()) +
                    " - Cantidad nueva: " + productoBD.getCantidad());
        }
    }

    @Override
    public Long estadoCancelado(EstadoPedido estadoPedido) {
        return repositorio.countByEstadoCancelado(EstadoPedido.CANCELADO);
    }

    @Override
    public Long estadoCEntregado(EstadoPedido estadoPedido) {
        return repositorio.countByEstadoEntregado(EstadoPedido.ENTREGADO);
    }
}
