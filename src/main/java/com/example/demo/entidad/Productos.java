package com.example.demo.entidad;

import com.example.demo.entidad.Enum.TipoVenta;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
    private BigDecimal cantidad = BigDecimal.ZERO;

    @ManyToOne
    @JoinColumn(name = "unidad_medida_id")
    private UnidadMedidas tipoVenta ;

    @Column(name = "cantidad_minima", precision = 10, scale = 2)
    private BigDecimal cantidadMinima;

    @Column(name = "incremento", precision = 10, scale = 2)
    private BigDecimal incremento;

    @Column(name = "stock_minimo", precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    @Column(name = "impuesto", precision = 10, scale = 2)
    private BigDecimal Impuesto = BigDecimal.ZERO;

    @Column(name = "precio_compra", precision = 10, scale = 2)
    private BigDecimal precioCompra;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id", nullable = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Proveedores proveedor;

    @Column(name = "precio_por_mayor", precision = 10, scale = 2)
    private BigDecimal PrecioPorMayor = BigDecimal.ZERO;

    @Column(name = "estado")
    private Boolean Estado = true;

    @ManyToOne
    @JoinColumn(name = "tributo_id")
    private Tributo tipo_Impuesto;

    // Constructores
    public Productos() {
    }



    public boolean tieneStockDisponible(BigDecimal cantidadSolicitada) {
        return this.cantidad.compareTo(cantidadSolicitada) >= 0;
    }

    public boolean necesitaReabastecimiento() {
        return stockMinimo != null && this.cantidad.compareTo(stockMinimo) <= 0;
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

    public UnidadMedidas getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(UnidadMedidas tipoVenta) {
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

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public BigDecimal getPrecioPorMayor() {
        return PrecioPorMayor;
    }

    public void setPrecioPorMayor(BigDecimal precioPorMayor) {
        PrecioPorMayor = precioPorMayor;
    }

    public Boolean getEstado() {
        return Estado;
    }

    public void setEstado(Boolean estado) {
        Estado = estado;
    }

    public Tributo getTipo_Impuesto() {
        return tipo_Impuesto;
    }

    public void setTipo_Impuesto(Tributo tipo_Impuesto) {
        this.tipo_Impuesto = tipo_Impuesto;
    }
}