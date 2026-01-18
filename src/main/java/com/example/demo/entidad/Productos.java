package com.example.demo.entidad;

import com.example.demo.entidad.Enum.TipoVenta;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Productos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "cantidad", nullable = false, scale = 2, precision = 10)
    private BigDecimal cantidad = BigDecimal.ZERO;  // ✅ Cambiar a BigDecimal para soportar decimales

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_venta", nullable = false)
    private TipoVenta tipoVenta = TipoVenta.UNIDAD;  // ✅ Por defecto venta por unidad

    @Column(name = "cantidad_minima", precision = 10, scale = 2)
    private BigDecimal cantidadMinima;  // ✅ Para definir venta mínima (ej: 0.5 kg)

    @Column(name = "incremento", precision = 10, scale = 2)
    private BigDecimal incremento;  // ✅ Para definir incrementos (ej: 0.25 kg)

    @Column(name = "stock_minimo", precision = 10, scale = 2)
    private BigDecimal stockMinimo;  // ✅ Alerta de stock bajo

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "impuesto", precision = 10, scale = 2)
    private BigDecimal Impuesto = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = true)
    private Proveedores proveedor;

    // Constructores
    public Productos() {
    }

    public Productos(String nombre, String descripcion, BigDecimal precio,
                     BigDecimal cantidad, Categoria categoria, TipoVenta tipoVenta, BigDecimal impuesto,Proveedores proveedor) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.categoria = categoria;
        this.tipoVenta = tipoVenta;
        this.Impuesto = impuesto;
        this.proveedor = proveedor;
    }

    // Métodos útiles
    public boolean esVentaPorPeso() {
        return this.tipoVenta == TipoVenta.PESO;
    }

    public boolean tieneStockDisponible(BigDecimal cantidadSolicitada) {
        return this.cantidad.compareTo(cantidadSolicitada) >= 0;
    }

    public boolean necesitaReabastecimiento() {
        return stockMinimo != null && this.cantidad.compareTo(stockMinimo) <= 0;
    }

    public String getUnidadMedida() {
        switch (tipoVenta) {
            case PESO: return "kg";
            case VOLUMEN: return "L";
            case METRO: return "m";
            default: return "unidad";
        }
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public TipoVenta getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(TipoVenta tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public BigDecimal getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(BigDecimal cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public BigDecimal getIncremento() {
        return incremento;
    }

    public void setIncremento(BigDecimal incremento) {
        this.incremento = incremento;
    }

    public BigDecimal getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(BigDecimal stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getImpuesto() {
        return Impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        Impuesto = impuesto;
    }

    public Proveedores getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedores proveedor) {
        this.proveedor = proveedor;
    }
}