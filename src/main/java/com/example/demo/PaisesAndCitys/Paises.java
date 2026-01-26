package com.example.demo.PaisesAndCitys;

import jakarta.persistence.*;

@Entity
@Table(name = "paises")
public class Paises {

    @Id
    private Integer id;

    @Column(name = "codigo",unique = true)
    private String codigo_iso;

    @Column(name = "nombre",unique = true)
    private String nombre;


    public Paises() {
    }

    public Paises(Integer id, String codigo_iso, String nombre) {
        this.id = id;
        this.codigo_iso = codigo_iso;
        this.nombre = nombre;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo_iso() {
        return codigo_iso;
    }

    public void setCodigo_iso(String codigo_iso) {
        this.codigo_iso = codigo_iso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
