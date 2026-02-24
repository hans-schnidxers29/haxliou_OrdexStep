package com.example.demo.entidad;

import com.example.demo.entidad.Enum.MetodoPago;
import jakarta.persistence.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name= "salio_caja")
    private boolean salioCaja = false ;

    @OneToMany(mappedBy = "abonosCompra", cascade = CascadeType.ALL)
    private List<Pagos> pagos =  new ArrayList<>() ;

    // Método de conveniencia para agregar pagos
    public void addPago(String metodo, BigDecimal monto) {
        if(monto == null ) monto = BigDecimal.ZERO;
        Pagos pago = new Pagos();
        pago.setMetodoPago(MetodoPago.valueOf(metodo));
        pago.setMonto(monto);
        pago.setAbonosCompra(this);
        this.pagos.add(pago);
    }
    public void limpiarPagos() {
        this.pagos.clear();
    }

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

    public boolean isSalioCaja() {
        return salioCaja;
    }

    public void setSalioCaja(boolean salioCaja) {
        this.salioCaja = salioCaja;
    }

    public List<Pagos> getPagos() {
        return pagos;
    }

    public void setPagos(List<Pagos> pagos) {
        this.pagos = pagos;
    }
}