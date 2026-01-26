package com.example.demo.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name ="unidad_medida")
public class UnidadMedidas {


    @Id
    private Integer id;

    @Column(name = "code", length = 10)
    private String Code;

    @Column(name = "name", length = 100)
    private String name ;

    public UnidadMedidas() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return Code;
    }

    public void setCode(String code) {
        Code = code;
    }





}
