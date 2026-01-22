package com.example.demo.servicio;

import com.example.demo.entidad.Compras;
import com.example.demo.entidad.DetalleCompra;
import com.example.demo.entidad.Enum.EstadoCompra;
import com.example.demo.entidad.Enum.TipoVenta;
import com.example.demo.repositorio.ComprasRepositorio;
import com.example.demo.repositorio.ProductoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompraServicioImp implements CompraServicio{

    @Autowired
    private ComprasRepositorio repositorio;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private ProductoRepositorio ProductoRepositorio;

    @Override
    public void saveCompra(Compras compras) {
        repositorio.save(compras);
    }

    @Override
    public List<Compras> listarCompra() {
        return repositorio.findAll().stream().toList();
    }

    @Override
    public Compras compraById(Long id) {
        return repositorio.findById(id).orElseThrow(
                ()-> new RuntimeException("compra no encontrado"));
    }

    @Override
    public void deleteCompraById(Long id) {
        repositorio.deleteById(id);
    }

    @Override
    @Transactional
    public void updateCompra(Long id, Compras comprasNuevas) {
        Compras compraExistente = compraById(id);

        if (compraExistente.getEstado() != EstadoCompra.BORRADOR) {
            throw new RuntimeException("No se puede editar una compra que ya ha sido confirmada.");
        }
        compraExistente.setProveedor(comprasNuevas.getProveedor());
        compraExistente.setTotal(comprasNuevas.getTotal());

        compraExistente.getDetalles().clear();

        if (comprasNuevas.getDetalles() != null) {
            for (DetalleCompra detalle : comprasNuevas.getDetalles()) {
                detalle.setCompra(compraExistente);
                compraExistente.getDetalles().add(detalle);
            }
        }

    }

    @Override
    public boolean verifcarCompra(Long id) {
        return false;
    }

    @Override
    public String GenerarReferenciasDeCompras() {
        Long siguienteReferencia = repositorio.obtenerNumeroSigReferencia();
        return "COMP-"+ String.format("%06d",siguienteReferencia);
    }

    @Override
    @Transactional
    public void ConfirmarCompra(Long id) {
        Compras compra = compraById(id);
        if(compra.getEstado() != EstadoCompra.BORRADOR){
            throw new IllegalStateException("La compra ya fue confirmada o anulada");
        }
        for (DetalleCompra detalleCompra : compra.getDetalles()) {
            // Obtenemos los valores que el usuario editó en el formulario de compra
            BigDecimal nuevoImpuesto = detalleCompra.getProductos().getImpuesto();
            BigDecimal nuevoPrecioCompra = detalleCompra.getProductos().getPrecioCompra(); // <-- CORREGIDO: Usar precioCompra
            
            productoServicio.AgregarStock(
                    detalleCompra.getProductos().getId(), 
                    detalleCompra.getCantidad(),
                    nuevoImpuesto,
                    nuevoPrecioCompra
                );
            }
            
            compra.setEstado(EstadoCompra.CONFIRMADA);
        }

    @Override
    @Transactional
    public void AnularCompra(Long id) {
        Compras compra = compraById(id);
        if(compra.getEstado() != EstadoCompra.BORRADOR){
            throw new IllegalStateException("La compra ya fue confirmada o anulada");
        }
        String referencia = compra.getNumeroReferencia();
        compra.setNumeroReferencia(referencia);
        compra.setEstado(EstadoCompra.ANULADA);
    }

    public Map<String, Object> StokMensual(LocalDateTime inicio, LocalDateTime fin) {
        Map<String, Object> datos = new HashMap<>();

        // Gasto total (este ya te funciona)
        datos.put("TotalEgresos", repositorio.sumTotalCompras(inicio, fin));

        // Cantidades (usando los nombres de los Enums)
        BigDecimal unidades = repositorio.sumarTotalEntrantePorTipoYRango(
                TipoVenta.UNIDAD.name(), // Convertir a String
                EstadoCompra.CONFIRMADA.name(), // Convertir a String
                inicio,
                fin
        );

        BigDecimal peso = repositorio.sumarTotalEntrantePorTipoYRango(
                TipoVenta.PESO.name(), // O el nombre exacto de tu enum para peso
                EstadoCompra.CONFIRMADA.name(),
                inicio,
                fin
        );

        datos.put("EntradasEnUnidades", unidades);
        datos.put("EntradasEnKg", peso);

        return datos;
     }
}
