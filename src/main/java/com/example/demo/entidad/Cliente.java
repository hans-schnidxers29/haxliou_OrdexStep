package com.example.demo.entidad;


import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "cliente")
@NamedQueries({
        @NamedQuery(name = "contadorPedidos", query = "select count(c) from Cliente c where c.pedidos is not empty", lockMode = LockModeType.OPTIMISTIC_FORCE_INCREMENT)
})
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre",nullable = false)
    private String nombre;


    @Column(name="email",nullable = false, unique = true)
    private String email;

    @Column(name="apellido",nullable = false)
    private String apellido;

    @Column(name="telefono", nullable = false)
    private String telefono;

    @Column(name="ciudad", nullable = false)
    private String ciudad;

    @Column(name="pais", nullable = false)
    private String pais;

    @Column(name="codigo_postal", nullable = false)
    private int codigoPostal;

    @OneToMany(mappedBy = "cliente", fetch=FetchType.LAZY, cascade= CascadeType.ALL )
    private List<Pedidos> pedidos = new ArrayList<>();



    public Cliente() {
    }

    public Cliente(String nombre, String email, String apellido, String telefono, String ciudad, String pais, int codigoPostal) {
        this.nombre = nombre;
        this.email = email;
        this.apellido = apellido;
        this.telefono = telefono;
        this.ciudad = ciudad;
        this.pais = pais;
        this.codigoPostal = codigoPostal;
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

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
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




    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "nombre = " + nombre + ", " +
                "email = " + email + ", " +
                "apellido = " + apellido + ", " +
                "telefono = " + telefono + ", " +
                "ciudad = " + ciudad + ", " +
                "pais = " + pais + ", " +
                "codigoPostal = " + codigoPostal + ")";
    }
}
