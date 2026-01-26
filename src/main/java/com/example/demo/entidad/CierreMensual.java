package com.example.demo.entidad;

import groovy.transform.builder.Builder;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cierre_mensual")
@Builder
public class CierreMensual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer mes;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    // ==================== MÉTODOS DE PAGO ====================
    @Column(precision = 19, scale = 4)
    private BigDecimal totalVentasEfectivo = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalVentasTarjeta = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalVentasTransferencia = BigDecimal.ZERO;

    @Column(precision = 19, scale = 4)
    private BigDecimal totalVentasMixto = BigDecimal.ZERO; // NUEVO: Para pagos mixtos

    @Column(precision = 19, scale = 4)
    private BigDecimal totalVentasOtros = BigDecimal.ZERO;

    // ==================== TOTALES FINANCIEROS ====================
    @Column(precision = 19, scale = 4)
    private BigDecimal recaudacionTotal = BigDecimal.ZERO; // Total de ventas incluyendo impuestos

    @Column(precision = 19, scale = 4)
    private BigDecimal totalImpuestos = BigDecimal.ZERO; // IVA y otros impuestos recaudados

    @Column(precision = 19, scale = 4)
    private BigDecimal recaudacionBruta = BigDecimal.ZERO; // Ventas sin impuestos (Netas)

    // ==================== COSTOS Y GASTOS ====================
    @Column(precision = 19, scale = 4)
    private BigDecimal costoVentas = BigDecimal.ZERO; //  Costo real de productos vendidos

    @Column(precision = 19, scale = 4)
    private BigDecimal totalCompras = BigDecimal.ZERO; // Inversión en nueva mercancía

    @Column(precision = 19, scale = 4)
    private BigDecimal totalEgresos = BigDecimal.ZERO; // Gastos operativos (luz, agua, salarios, etc)

    // ==================== UTILIDADES ====================
    @Column(precision = 19, scale = 4)
    private BigDecimal utilidadBruta = BigDecimal.ZERO; // NUEVO: Ventas Netas - Costo de Ventas

    @Column(precision = 19, scale = 4)
    private BigDecimal utilidadNeta = BigDecimal.ZERO; // Utilidad Bruta - Gastos Operativos

    @Column(precision = 19, scale = 4)
    private BigDecimal margenBruto = BigDecimal.ZERO; // NUEVO: % de ganancia (Utilidad Bruta / Ventas Netas * 100)

    @Column(precision = 19, scale = 4)
    private BigDecimal totalVentasAlMayor ;

    // ==================== ESTADÍSTICAS ====================
    private Integer cantidadPedidos = 0;
    private Integer nuevosClientes = 0;
    private Integer cantidadVentas = 0; // NUEVO: Total de facturas emitidas

    // ==================== INVENTARIO ====================
    private Integer totalProductosEnStock = 0;

    @Column(precision = 19, scale = 4)
    private BigDecimal valorInventarioTotal = BigDecimal.ZERO;

    // ==================== CONSTRUCTORES ====================
    public CierreMensual() {
    }

    public CierreMensual(Long id, Integer mes, Integer anio, LocalDate fechaCierre,
                         BigDecimal totalVentasEfectivo, BigDecimal totalVentasTarjeta,
                         BigDecimal totalVentasTransferencia, BigDecimal totalVentasOtros,
                         BigDecimal recaudacionBruta, BigDecimal totalImpuestos,
                         BigDecimal recaudacionTotal, BigDecimal totalEgresos,
                         BigDecimal utilidadNeta, BigDecimal totalCompras,
                         Integer cantidadPedidos) {
        this.id = id;
        this.mes = mes;
        this.anio = anio;
        this.fechaCierre = fechaCierre;
        this.totalVentasEfectivo = totalVentasEfectivo;
        this.totalVentasTarjeta = totalVentasTarjeta;
        this.totalVentasTransferencia = totalVentasTransferencia;
        this.totalVentasOtros = totalVentasOtros;
        this.recaudacionBruta = recaudacionBruta;
        this.totalImpuestos = totalImpuestos;
        this.recaudacionTotal = recaudacionTotal;
        this.totalEgresos = totalEgresos;
        this.utilidadNeta = utilidadNeta;
        this.totalCompras = totalCompras;
        this.cantidadPedidos = cantidadPedidos;
    }

    // ==================== MÉTODOS DE UTILIDAD ====================
    /**
     * Retorna el nombre del mes en español
     */
    public String getNombreMes() {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return (mes != null && mes >= 1 && mes <= 12) ? meses[mes - 1] : "Desconocido";
    }

    /**
     * Retorna el período en formato legible: "Enero 2025"
     */
    public String getPeriodoFormateado() {
        return getNombreMes() + " " + anio;
    }

    /**
     * Calcula el margen de ganancia bruta en porcentaje
     */
    public BigDecimal calcularMargenBruto() {
        if (recaudacionBruta != null && recaudacionBruta.compareTo(BigDecimal.ZERO) > 0) {
            return utilidadBruta.divide(recaudacionBruta, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    /**
     * Calcula el margen de ganancia neta en porcentaje
     */
    public BigDecimal calcularMargenNeto() {
        if (recaudacionBruta != null && recaudacionBruta.compareTo(BigDecimal.ZERO) > 0) {
            return utilidadNeta.divide(recaudacionBruta, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }
        return BigDecimal.ZERO;
    }

    // ==================== GETTERS Y SETTERS ====================
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public LocalDate getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDate fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getTotalVentasEfectivo() {
        return totalVentasEfectivo;
    }

    public void setTotalVentasEfectivo(BigDecimal totalVentasEfectivo) {
        this.totalVentasEfectivo = totalVentasEfectivo;
    }

    public BigDecimal getTotalVentasTarjeta() {
        return totalVentasTarjeta;
    }

    public void setTotalVentasTarjeta(BigDecimal totalVentasTarjeta) {
        this.totalVentasTarjeta = totalVentasTarjeta;
    }

    public BigDecimal getTotalVentasTransferencia() {
        return totalVentasTransferencia;
    }

    public void setTotalVentasTransferencia(BigDecimal totalVentasTransferencia) {
        this.totalVentasTransferencia = totalVentasTransferencia;
    }

    public BigDecimal getTotalVentasMixto() {
        return totalVentasMixto;
    }

    public void setTotalVentasMixto(BigDecimal totalVentasMixto) {
        this.totalVentasMixto = totalVentasMixto;
    }

    public BigDecimal getTotalVentasOtros() {
        return totalVentasOtros;
    }

    public void setTotalVentasOtros(BigDecimal totalVentasOtros) {
        this.totalVentasOtros = totalVentasOtros;
    }

    public BigDecimal getRecaudacionBruta() {
        return recaudacionBruta;
    }

    public void setRecaudacionBruta(BigDecimal recaudacionBruta) {
        this.recaudacionBruta = recaudacionBruta;
    }

    public BigDecimal getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(BigDecimal totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    public BigDecimal getRecaudacionTotal() {
        return recaudacionTotal;
    }

    public void setRecaudacionTotal(BigDecimal recaudacionTotal) {
        this.recaudacionTotal = recaudacionTotal;
    }

    public BigDecimal getCostoVentas() {
        return costoVentas;
    }

    public void setCostoVentas(BigDecimal costoVentas) {
        this.costoVentas = costoVentas;
    }

    public BigDecimal getTotalEgresos() {
        return totalEgresos;
    }

    public void setTotalEgresos(BigDecimal totalEgresos) {
        this.totalEgresos = totalEgresos;
    }

    public BigDecimal getUtilidadBruta() {
        return utilidadBruta;
    }

    public void setUtilidadBruta(BigDecimal utilidadBruta) {
        this.utilidadBruta = utilidadBruta;
    }

    public BigDecimal getUtilidadNeta() {
        return utilidadNeta;
    }

    public void setUtilidadNeta(BigDecimal utilidadNeta) {
        this.utilidadNeta = utilidadNeta;
    }

    public BigDecimal getMargenBruto() {
        return margenBruto;
    }

    public void setMargenBruto(BigDecimal margenBruto) {
        this.margenBruto = margenBruto;
    }

    public BigDecimal getTotalCompras() {
        return totalCompras;
    }

    public void setTotalCompras(BigDecimal totalCompras) {
        this.totalCompras = totalCompras;
    }

    public Integer getCantidadPedidos() {
        return cantidadPedidos;
    }

    public void setCantidadPedidos(Integer cantidadPedidos) {
        this.cantidadPedidos = cantidadPedidos;
    }

    public Integer getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Integer cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public Integer getNuevosClientes() {
        return nuevosClientes;
    }

    public void setNuevosClientes(Integer nuevosClientes) {
        this.nuevosClientes = nuevosClientes;
    }

    public Integer getTotalProductosEnStock() {
        return totalProductosEnStock;
    }

    public void setTotalProductosEnStock(Integer totalProductosEnStock) {
        this.totalProductosEnStock = totalProductosEnStock;
    }

    public BigDecimal getValorInventarioTotal() {
        return valorInventarioTotal;
    }

    public void setValorInventarioTotal(BigDecimal valorInventarioTotal) {
        this.valorInventarioTotal = valorInventarioTotal;
    }

    public BigDecimal getTotalVentasAlMayor() {
        return totalVentasAlMayor;
    }

    public void setTotalVentasAlMayor(BigDecimal totalVentasAlMayor) {
        this.totalVentasAlMayor = totalVentasAlMayor;
    }
}