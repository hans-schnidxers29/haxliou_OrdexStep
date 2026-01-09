package com.example.demo.servicio;

import com.example.demo.entidad.DetallePedido;
import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.entidad.Pedidos;
import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.PedidoRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        pedidos1.setFlete(pedidos.getFlete());
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

            // 1. Validaciones de seguridad
            if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                continue;
            }

            // 2. Validación de cantidad: detalle.getCantidad() > 0
            if (detalle.getCantidad() == null || detalle.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            // 3. Buscar el producto completo desde la BD
            Productos productoBD = productoServicio.productoById(detalle.getProducto().getId());

            // 4. Verificar stock suficiente: productoBD.getCantidad() < detalle.getCantidad()
            // compareTo devuelve -1 si el stock en BD es menor a lo solicitado
            if (productoBD.getCantidad().compareTo(detalle.getCantidad()) < 0) {
                throw new RuntimeException("Stock insuficiente para el producto: " + productoBD.getNombre() +
                        ". Disponible: " + productoBD.getCantidad() +
                        ", Solicitado: " + detalle.getCantidad());
            }

            // 5. Guardar cantidad original para el registro (Log)
            BigDecimal cantidadAnterior = productoBD.getCantidad();

            // 6. Descontar stock: productoBD.getCantidad() - detalle.getCantidad()
            BigDecimal nuevaCantidad = productoBD.getCantidad().subtract(detalle.getCantidad());
            productoBD.setCantidad(nuevaCantidad);

            // 7. Sincronizar objetos
            detalle.setProducto(productoBD);

            // 8. Persistir cambios
            productoServicio.save(productoBD);

            System.out.println("Stock actualizado (Pedido) - Producto: " + productoBD.getNombre() +
                    " - Cantidad anterior: " + cantidadAnterior +
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

    @Override
    @Transactional
    public void EntregarPedido(Long id) {
        Pedidos pedido = repositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        // Validaciones
        if (pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new IllegalStateException("El pedido ya está entregado");
        }

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se puede entregar un pedido cancelado");
        }

        EstadoPedido estadoAnterior = pedido.getEstado();

        // Descontar stock si es necesario
        if (estadoAnterior != EstadoPedido.ENTREGADO) {
            DescantorStock(pedido);
        }
        // Cambiar el estado
        pedido.setEstado(EstadoPedido.ENTREGADO);
    }

    @Transactional
    @Override
    public void CancelarPedido(Long id) {
     Pedidos pedido =   repositorio.findById(id).orElseThrow(() -> new RuntimeException("Pedido no encontrado"));

        if(pedido.getEstado()==EstadoPedido.CANCELADO){
            throw new IllegalStateException("el Pedido ya fue Cancelado");
        }

        if(pedido.getEstado()==EstadoPedido.ENTREGADO){
            throw  new IllegalStateException("el Pedido ya fue Entregado");
        }

        pedido.setEstado(EstadoPedido.CANCELADO);
    }

}
