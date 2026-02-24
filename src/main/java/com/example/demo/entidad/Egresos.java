package com.example.demo.entidad;


import com.example.demo.entidad.Enum.MetodoPago;
import com.example.demo.entidad.Enum.TipoEgreso;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "egresos")
public class Egresos {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TipoEgreso tipoEgreso;

    @Column(name = "monto",scale = 2, precision = 10)
    private BigDecimal monto;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(columnDefinition = "TEXT", name = "descripcion_gasto")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    @Column(name = "salio_caja")
    private boolean salioCaja = false ;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa ;

    @OneToMany(mappedBy = "egresos", cascade = CascadeType.ALL)
    private List<Pagos> pagos = new ArrayList<>();

    // Método de conveniencia para agregar pagos
    public void addPago(String metodo, BigDecimal monto) {
        if(monto == null ) monto = BigDecimal.ZERO;
        Pagos pago = new Pagos();
        pago.setMetodoPago(MetodoPago.valueOf(metodo));
        pago.setMonto(monto);
        pago.setEgresos(this);
        this.pagos.add(pago);
    }
    public void limpiarPagos() {
        this.pagos.clear();
    }

    public Egresos() {
    }

    public Egresos(Long id, TipoEgreso tipoEgreso, BigDecimal monto, LocalDateTime fechaRegistro, Usuario usuario, String descripcion) {
        this.id = id;
        this.tipoEgreso = tipoEgreso;
        this.monto = monto;
        this.fechaRegistro = fechaRegistro;
        this.usuario = usuario;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoEgreso getTipoEgreso() {
        return tipoEgreso;
    }

    public void setTipoEgreso(TipoEgreso tipoEgreso) {
        this.tipoEgreso = tipoEgreso;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
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
