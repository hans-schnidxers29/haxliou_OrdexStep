package com.example.demo.entidad;


import com.example.demo.entidad.Enum.EstadoPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;



@Entity
@Table(name = "pedidos")
public class Pedidos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido = LocalDateTime.now();

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDateTime fechaEntrega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "impuesto", precision = 10, scale = 2, nullable = false)
    private BigDecimal impuesto = BigDecimal.ZERO;

    @Column(name = "total", precision = 10, scale = 2, nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "Flete", precision = 10, scale = 2)
    private BigDecimal Flete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    @PreUpdate
    public void calcularTotales() {
        // 1. Inicializar subtotal de productos
        this.subtotal = BigDecimal.ZERO;
        if (this.detalles != null) {
            for (DetallePedido detalle : detalles) {
                this.subtotal = this.subtotal.add(detalle.getSubtotal());
            }
        }
        BigDecimal valorFlete = (this.Flete != null) ? this.Flete : BigDecimal.ZERO;
        BigDecimal porcentajeImpuesto = (this.impuesto != null) ? this.impuesto : BigDecimal.ZERO;

        BigDecimal baseImponible = this.subtotal.add(valorFlete);

        // 4. Calcular el MONTO del impuesto (Base * Porcentaje / 100)
        BigDecimal montoImpuesto = baseImponible.multiply(porcentajeImpuesto)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        // 5. Total Final = Base + Impuesto
        this.total = baseImponible.add(montoImpuesto).setScale(2, RoundingMode.HALF_UP);
    }

    public Pedidos() {
    }

    // Métodos helper
    public void addDetalle(DetallePedido detalle) {
        detalles.add(detalle);
        detalle.setPedido(this);
    }

    public void removeDetalle(DetallePedido detalle) {
        detalles.remove(detalle);
        detalle.setPedido(null);
    }

    public void actualizarDetalles(List<DetallePedido> nuevosDetalles) {
        this.detalles.clear(); // Limpia los anteriores
        if (nuevosDetalles != null) {
            for (DetallePedido detalle : nuevosDetalles) {
                detalle.setPedido(this); // Importante: Vincular de vuelta al pedido
                this.detalles.add(detalle);
            }
        }
    }



    public BigDecimal getFlete() {
        return Flete;
    }

    public void setFlete(BigDecimal flete) {
        Flete = flete;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
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

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<DetallePedido> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedido> detalles) {
        this.detalles = detalles;
    }
}
