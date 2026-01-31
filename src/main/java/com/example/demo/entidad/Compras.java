package com.example.demo.entidad;


import com.example.demo.entidad.Enum.EstadoCompra;
import com.example.demo.entidad.Enum.MetodoPago;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "compras")
public class Compras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_referencia")
    private String numeroReferencia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedores Proveedor;

    @Column(name = "fecha_compra")
    private LocalDateTime fechaCompra = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private EstadoCompra estado= EstadoCompra.BORRADOR;

    @Column(name = "total", scale = 2, precision = 10)
    private BigDecimal total = BigDecimal.ZERO;

    // mappedBy corregido a "compra" (singular)
    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles = new ArrayList<>();

    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @ManyToOne
    @JoinColumn(name="empresa_id")
    private Empresa empresa ;


    // MÉTODO HELPER: Muy importante para que el cascade funcione
    public void agregarDetalle(DetalleCompra detalle) {
        detalles.add(detalle);
        detalle.setCompra(this);
    }
    public Compras() {
    }

    public Compras(Long id, String numeroReferencia, Proveedores proveedor,
                   LocalDateTime fechaCompra, Usuario usuario, EstadoCompra estado, BigDecimal total,
                   List<DetalleCompra> detalles,String observaciones,MetodoPago metodoPago) {
        this.id = id;
        this.numeroReferencia = numeroReferencia;
        Proveedor = proveedor;
        this.fechaCompra = fechaCompra;
        this.usuario = usuario;
        this.estado = estado;
        this.total = total;
        this.detalles = detalles;
        this.observaciones=observaciones;
        this.metodoPago=metodoPago;
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

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}
