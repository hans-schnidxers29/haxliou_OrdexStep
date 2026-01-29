package com.example.demo.entidad;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Column(nullable = false)
    private BigDecimal impuesto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago;

    @ManyToOne
    @JoinColumn(name = "vendedor_id", nullable = false)
    private Usuario Vendedor;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalles = new ArrayList<>();

    @Column(name = "venta_al_por_mayor")
    private Boolean VentaAlPorMayor = false;

    public Venta() {
    }

    public Venta(Long id, Cliente cliente, Usuario Vendedor,LocalDateTime fechaVenta, BigDecimal impuesto,
                 BigDecimal total, BigDecimal subtotal, String metodoPago, List<DetalleVenta> detalles) {
        this.id = id;
        this.cliente = cliente;
        this.fechaVenta = fechaVenta;
        this.impuesto = impuesto;
        this.total = total;
        this.subtotal = subtotal;
        this.metodoPago = metodoPago;
        this.detalles = detalles;
        this.Vendedor = Vendedor;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    public Usuario getVendedor() {
        return Vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        Vendedor = vendedor;
    }

    public void setVentaAlPorMayor(Boolean ventaAlPorMayor) {
        VentaAlPorMayor = ventaAlPorMayor;
    }

    public Boolean getVentaAlPorMayor() {
        return VentaAlPorMayor;
    }

}
