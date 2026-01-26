package com.example.demo.PaisesAndCitys;

import jakarta.persistence.*;

@Entity
@Table(name ="municipios")
public class Municipios {
    @Id
    private Integer id;

    @Column(name = "code", length = 10)
    private String code;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "department", length = 100)
    private String department;

    @ManyToOne
    @JoinColumn(name = "pais_id")
    private Paises pais;

    public Municipios() {
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Paises getPais() {
        return pais;
    }

    public void setPais(Paises pais) {
        this.pais = pais;
    }
}
