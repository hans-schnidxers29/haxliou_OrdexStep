package com.example.demo.entidad;


import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Enum.EstadoCompra;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "compras")
public class Compras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_referencia", nullable = false)
    private String numeroReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedores Proveedor;

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "usuario_id")
   private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado ;

    @Column(name = "total", scale = 3, precision = 10)
    private BigDecimal total;

    public Compras() {
    }

    public Compras(Long id, String numeroReferencia, Proveedores proveedor,
                 LocalDateTime  fechaCompra, Usuario usuario, EstadoCompra estado, BigDecimal total) {
        this.id = id;
        this.numeroReferencia = numeroReferencia;
        Proveedor = proveedor;
        this.fechaCompra = fechaCompra;
        this.usuario = usuario;
        this.estado = estado;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroReferencia() {
        return numeroReferencia;
    }

    public void setNumeroReferencia(String numeroReferencia) {
        this.numeroReferencia = numeroReferencia;
    }

    public Proveedores getProveedor() {
        return Proveedor;
    }

    public void setProveedor(Proveedores proveedor) {
        Proveedor = proveedor;
    }

    public LocalDateTime getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDateTime fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public void setEstado(EstadoCompra estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
