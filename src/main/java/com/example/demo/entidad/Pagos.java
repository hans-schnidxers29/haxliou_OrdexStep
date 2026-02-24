package com.example.demo.entidad;

import com.example.demo.entidad.Enum.MetodoPago;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
public class Pagos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago; // EFECTIVO, TARJETA, TRANSFERENCIA, etc.

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago")
    private LocalDateTime fechaPago = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "abonos_id")
    private AbonosCompra abonosCompra;

    @ManyToOne
    @JoinColumn(name = "egresos_id")
    private Egresos egresos;

    public Pagos() {
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public AbonosCompra getAbonosCompra() {
        return abonosCompra;
    }

    public void setAbonosCompra(AbonosCompra abonosCompra) {
        this.abonosCompra = abonosCompra;
    }

    public Egresos getEgresos() {
        return egresos;
    }

    public void setEgresos(Egresos egresos) {
        this.egresos = egresos;
    }
}
