package com.example.demo.PaisesAndCitys;

import jakarta.persistence.*;

@Entity
@Table(name = "paises")
public class Paises {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "codigo_iso",unique = true)
    private String codigo_iso;

    @Column(name = "nombre",unique = true)
    private String nombre;


    public Paises() {
    }

    public Paises(Long id, String codigo_iso, String nombre) {
        this.id = id;
        this.codigo_iso = codigo_iso;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
