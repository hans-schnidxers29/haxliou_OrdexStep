package com.example.demo.entidad;

import com.example.demo.entidad.Enum.EstadoPedido;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras_por_pagar")
public class ComprasCreditos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación uno a uno con la compra que generó la deuda
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compras compra;

    @Column(name = "monto_total", nullable = false)
    private BigDecimal montoTotal;

    @Column(name = "saldo_pendiente", nullable = false)
    private BigDecimal saldoPendiente;

    @Column(name = "fecha_vencimiento")
    private LocalDate fechaVencimiento;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoDeuda = EstadoPedido.PENDIENTE; // PENDIENTE, PAGADO, VENCIDO

    @OneToMany(mappedBy = "cuentaPorPagar", cascade = CascadeType.ALL)
    private List<AbonosCompra> abonos = new ArrayList<>();

    public ComprasCreditos() {
    }

    public List<AbonosCompra> getAbonos() {
        return abonos;
    }

    public void setAbonos(List<AbonosCompra> abonos) {
        this.abonos = abonos;
    }

    public Compras getCompra() {
        return compra;
    }

    public void setCompra(Compras compra) {
        this.compra = compra;
    }

    public EstadoPedido getEstadoDeuda() {
        return estadoDeuda;
    }

    public void setEstadoDeuda(EstadoPedido estadoDeuda) {
        this.estadoDeuda = estadoDeuda;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public BigDecimal getSaldoPendiente() {
        return saldoPendiente;
    }

    public void setSaldoPendiente(BigDecimal saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }
}
