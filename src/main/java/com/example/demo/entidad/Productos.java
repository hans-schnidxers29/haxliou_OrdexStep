package com.example.demo.entidad;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name="productos")
public class Productos {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="nombre",nullable = false)
    private String nombre;

    private String Descripcion;

    @Column(name="precio", nullable = false)
    private BigDecimal precio;

    @Column(name="cantidad", nullable = false)
    private Integer cantidad = 0;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;


    public Productos (String nombre, String descripcion, BigDecimal precio, Integer cantidad,Categoria categoria) {
        this.nombre = nombre;
        Descripcion = descripcion;
        this.precio = precio;
        this.cantidad = cantidad;
        this.categoria= categoria;
    }

    public Productos() {
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
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

    public String getDescripcion() {
        return Descripcion;
    }

    public void setDescripcion(String descripcion) {
        Descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
}
