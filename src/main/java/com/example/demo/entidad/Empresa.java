package com.example.demo.entidad;

import com.example.demo.Login.Usuario;
import jakarta.persistence.*;

@Entity
@Table(name = "empresa")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // --- DATOS DE LA EMPRESA ---
    @Column(name = "nit", unique = true)
    private String nit;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "razon_social")
    private String razonSocial;

    @Column(name = "direccion_empresa")
    private String direccionEmpresa;

    @Column(name = "actividad_economica")
    private String actividadEconomica;

    @Column(name = "tipo_operacion") // Ejemplo: Venta, Servicio, etc.
    private String tipoOperacion;

    @Column(name = "numero_telefono")
    private Integer numeroTelefono;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario Propietario;

    public Empresa() {

    }

    public Empresa(Long id, String nit,String nombre,Integer numeroTelefono, String razonSocial, String direccionEmpresa, String actividadEconomica, String tipoOperacion, Usuario propietario) {
        this.id = id;
        this.nit = nit;
        this.nombre = nombre;
        this.numeroTelefono = numeroTelefono;
        this.razonSocial = razonSocial;
        this.direccionEmpresa = direccionEmpresa;
        this.actividadEconomica = actividadEconomica;
        this.tipoOperacion = tipoOperacion;
        Propietario = propietario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getDireccionEmpresa() {
        return direccionEmpresa;
    }

    public void setDireccionEmpresa(String direccionEmpresa) {
        this.direccionEmpresa = direccionEmpresa;
    }

    public String getActividadEconomica() {
        return actividadEconomica;
    }

    public void setActividadEconomica(String actividadEconomica) {
        this.actividadEconomica = actividadEconomica;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public Usuario getPropietario() {
        return Propietario;
    }

    public void setPropietario(Usuario propietario) {
        Propietario = propietario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(Integer numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }
}

