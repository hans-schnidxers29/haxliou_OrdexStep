package com.example.demo.entidad;

import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Enum.EstadoDeCaja;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cierre_caja")
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre = LocalDateTime.now();

    @NotNull
    @Column(name="monto_inicial", scale = 3, precision = 10)
    private BigDecimal montoInicial = BigDecimal.ZERO;

    @NotNull
    @Column(name = "ingreso_total", scale = 3, precision = 10)
    private BigDecimal ingresoTotal = BigDecimal.ZERO;

    @Column(name = "egresos_totales", scale = 3, precision = 10)
    private BigDecimal egresosTotales = BigDecimal.ZERO;

    @Column(name = "gastos_totales", scale = 3, precision = 10)
    private BigDecimal gastosTotales = BigDecimal.ZERO;

    @Column(name = "monto_real", scale = 3, precision = 10)
    private BigDecimal montoReal = BigDecimal.ZERO;

    @Column(name = "diferencia", scale = 3, precision = 10)
    private BigDecimal diferencia = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EstadoDeCaja Estado;

    @Column(name="fecha_apertura")
    private LocalDateTime fechaApertura = LocalDateTime.now();

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "usuario_id")
//    private Usuario usuario;

    @Column(length = 500)
    private String observaciones;

    // Métodos útiles para el servicio
    public BigDecimal getMontoEsperado() {
        return montoInicial.add(ingresoTotal)
                .subtract(egresosTotales)
                .subtract(gastosTotales);
    }

    public Caja() {
    }

    public Caja(Long id, LocalDateTime fechaCierre, BigDecimal montoInicial, BigDecimal ingresoTotal,
                BigDecimal egresosTotales, BigDecimal gastosTotales, BigDecimal montoReal,
                BigDecimal diferencia, Usuario usuario, String observaciones, EstadoDeCaja Estado,LocalDateTime fechaApertura) {
        this.id = id;
        this.fechaCierre = fechaCierre;
        this.montoInicial = montoInicial;
        this.ingresoTotal = ingresoTotal;
        this.egresosTotales = egresosTotales;
        this.gastosTotales = gastosTotales;
        this.montoReal = montoReal;
        this.diferencia = diferencia;
        this.usuario = usuario;
        this.observaciones = observaciones;
        this.Estado = Estado;
        this.fechaApertura = fechaApertura;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public BigDecimal getIngresoTotal() {
        return ingresoTotal;
    }

    public void setIngresoTotal(BigDecimal ingresoTotal) {
        this.ingresoTotal = ingresoTotal;
    }

    public BigDecimal getEgresosTotales() {
        return egresosTotales;
    }

    public void setEgresosTotales(BigDecimal egresosTotales) {
        this.egresosTotales = egresosTotales;
    }

    public BigDecimal getGastosTotales() {
        return gastosTotales;
    }

    public void setGastosTotales(BigDecimal gastosTotales) {
        this.gastosTotales = gastosTotales;
    }

    public BigDecimal getMontoReal() {
        return montoReal;
    }

    public void setMontoReal(BigDecimal montoReal) {
        this.montoReal = montoReal;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public EstadoDeCaja getEstado() {
        return Estado;
    }

    public void setEstado(EstadoDeCaja estado) {
        Estado = estado;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }
}
