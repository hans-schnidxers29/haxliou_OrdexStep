package com.example.demo.entidad;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "venta")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private Productos producto;

    @Column(name = "fecha_venta")
    private LocalDateTime fechaVenta = LocalDateTime.now();

    @Column(name = "precio")
    private BigDecimal precio;

    @Column(name = "descuento")
    private BigDecimal descuento;

    @Column(name = "total")
    private BigDecimal total;


    public Venta() {
    }

    public Venta(Cliente cliente, Productos producto, LocalDateTime fechaVenta, BigDecimal precio, BigDecimal descuento, BigDecimal total) {
        this.cliente = cliente;
        this.producto = producto;
        this.fechaVenta = fechaVenta;
        this.precio = precio;
        this.descuento = descuento;
        this.total = total;
    }

    public Venta(Long id, Cliente cliente, Productos producto, LocalDateTime fechaVenta, BigDecimal precio, BigDecimal descuento, BigDecimal total) {
        this.id = id;
        this.cliente = cliente;
        this.producto = producto;
        this.fechaVenta = fechaVenta;
        this.precio = precio;
        this.descuento = descuento;
        this.total = total;
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

    public Productos getProducto() {
        return producto;
    }

    public void setProducto(Productos producto) {
        this.producto = producto;
    }

    public LocalDateTime getFechaVenta() {
        return fechaVenta;
    }

    public void setFechaVenta(LocalDateTime fechaVenta) {
        this.fechaVenta = fechaVenta;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
