package com.example.demo.entidad;


import com.example.demo.PaisesAndCitys.Municipios;
import com.example.demo.PaisesAndCitys.Paises;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="tipoIdentificacion", nullable = false)
    private String tipoidentificacion ;

    @Column(name="numero_identificacion",nullable = false, unique = true)
    private String numeroIdentificacion ;

    @Column(name="nombre",nullable = false)
    private String nombre;

    @Column(name="email",nullable = false)
    private String email;

    @Column(name="apellido",nullable = false)
    private String apellido;

    @Column(name="telefono", nullable = false)
    private String telefono;

    @ManyToOne
    @JoinColumn(name = "municipio_id")
    private Municipios ciudad;

    @ManyToOne
    @JoinColumn(name = "pais_id")
    private Paises pais;

    @Column(name="codigo_postal", nullable = false)
    private int codigoPostal;

    @Column(name="fecha_registro")
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "cliente", fetch=FetchType.LAZY, cascade= CascadeType.ALL )
    @JsonManagedReference
    private List<Pedidos> pedidos = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String direccion ;

    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    public Cliente() {
    }

    public Cliente(String apellido, Municipios ciudad, int codigoPostal, String direccion,
                   String email, LocalDateTime fechaRegistro, Long id, String nombre, String numeroIdentificacion,
                   Paises pais, List<Pedidos> pedidos, String telefono, String tipoidentificacion) {
        this.apellido = apellido;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
        this.direccion = direccion;
        this.email = email;
        this.fechaRegistro = fechaRegistro;
        this.id = id;
        this.nombre = nombre;
        this.numeroIdentificacion = numeroIdentificacion;
        this.pais = pais;
        this.pedidos = pedidos;
        this.telefono = telefono;
        this.tipoidentificacion = tipoidentificacion;
    }

    public List<Pedidos> getPedidos() {
        return pedidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Municipios getCiudad() {
        return ciudad;
    }

    public void setCiudad(Municipios ciudad) {
        this.ciudad = ciudad;
    }

    public Paises getPais() {
        return pais;
    }

    public void setPais(Paises pais) {
        this.pais = pais;
    }

    public int getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(int codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setPedidos(List<Pedidos> pedidos) {
        this.pedidos = pedidos;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoidentificacion() {
        return tipoidentificacion;
    }

    public void setTipoidentificacion(String tipoidentificacion) {
        this.tipoidentificacion = tipoidentificacion;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass().hashCode() : getClass().hashCode();
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", TipoIdentificacion='" + tipoidentificacion + '\'' +
                ", NumeroIdentificacion='" + numeroIdentificacion + '\'' +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", apellido='" + apellido + '\'' +
                ", telefono='" + telefono + '\'' +
                ", ciudad='" + ciudad + '\'' +
                ", pais='" + pais + '\'' +
                ", codigoPostal=" + codigoPostal +
                ", pedidos=" + pedidos +
                ", direccion='" + direccion + '\'' +
                '}';
    }
}
