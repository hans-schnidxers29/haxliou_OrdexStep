package com.example.demo.entidad;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "detalle_compra")
public class DetalleCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="compras_id")
    private Compras compra;

    @ManyToOne
    @JoinColumn(name="productos_id")
    private Productos productos;

    @Column(name="cantidad",scale = 3, precision = 10)
    private BigDecimal cantidad;

    @Column(name="precio_unitario",scale = 3, precision = 10)
    private BigDecimal precioUnitario;

    @Column(name="subtotal",scale = 3, precision = 10)
    private BigDecimal subtotal;

    @Column(name="impuesto",scale = 2, precision = 10)
    private BigDecimal impuesto;

    public DetalleCompra() {
    }

    public DetalleCompra(Long id, Compras compra, Productos productos,
                         BigDecimal cantidad, BigDecimal precioUnitario, BigDecimal subtotal, BigDecimal impuesto) {
        this.id = id;
        this.compra = compra;
        this.productos = productos;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.impuesto = impuesto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Compras getCompra() {
        return compra;
    }

    public void setCompra(Compras compra) {
        this.compra = compra;
    }

    public Productos getProductos() {
        return productos;
    }

    public void setProductos(Productos productos) {
        this.productos = productos;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
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
}
