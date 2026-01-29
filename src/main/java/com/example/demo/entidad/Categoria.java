package com.example.demo.entidad;


import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombrecategoria")
    private String nombrecategoria;

    @Column(name = "decripcion")
    private String descripcion;


    @ManyToOne
    @JoinColumn(name = "empresa_id")
    private Empresa empresa ;

    public Categoria() {
    }

    public Categoria(String nombrecategoria, String descripcion) {
        this.nombrecategoria = nombrecategoria;
        this.descripcion = descripcion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombrecategoria() {
        return nombrecategoria;
    }

    public void setNombrecategoria(String nombrecategoria) {
        this.nombrecategoria = nombrecategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
    }
}
