package com.example.demo.entidad;

import com.example.demo.entidad.Enum.MetodoPago;
import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "abonos_compra")
public class AbonosCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_pagar_id", nullable = false)
    private ComprasCreditos cuentaPorPagar;

    @Column(name = "monto_abonado", nullable = false)
    private BigDecimal montoAbonado;

    @Column(name = "fecha_abono")
    private LocalDateTime fechaAbono = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago; // Puede abonar en EFECTIVO o TRANSFERENCIA

    public AbonosCompra() {
    }

    public ComprasCreditos getCuentaPorPagar() {
        return cuentaPorPagar;
    }

    public void setCuentaPorPagar(ComprasCreditos cuentaPorPagar) {
        this.cuentaPorPagar = cuentaPorPagar;
    }

    public LocalDateTime getFechaAbono() {
        return fechaAbono;
    }

    public void setFechaAbono(LocalDateTime fechaAbono) {
        this.fechaAbono = fechaAbono;
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

    public BigDecimal getMontoAbonado() {
        return montoAbonado;
    }

    public void setMontoAbonado(BigDecimal montoAbonado) {
        this.montoAbonado = montoAbonado;
    }
}