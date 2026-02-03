package com.example.demo.entidad;

import jakarta.persistence.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

@Entity
@Table(name = "empresa")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
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
    private String numeroTelefono;

    @OneToMany(mappedBy ="empresa" )
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<UsuarioEmpresa> usuario;   

    public Empresa() {

    }

    public Empresa(Long id, String nit, String nombre, String numeroTelefono, String razonSocial,
                   String direccionEmpresa, String actividadEconomica, String tipoOperacion,
                   List<UsuarioEmpresa> user) {
        this.id = id;
        this.nit = nit;
        this.nombre = nombre;
        this.numeroTelefono = numeroTelefono;
        this.razonSocial = razonSocial;
        this.direccionEmpresa = direccionEmpresa;
        this.actividadEconomica = actividadEconomica;
        this.tipoOperacion = tipoOperacion;
        this.usuario = user;
    }

    // ... existing code ...

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    // ... existing code ...

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

    public List<UsuarioEmpresa> getUsuario() {
        return usuario;
    }

    public void setUsuario(List<UsuarioEmpresa> usuario) {
        this.usuario = usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }
}

