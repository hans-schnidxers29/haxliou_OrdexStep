package com.example.demo.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tributos")
public class Tributo {
    @Id
    private Integer id; // ID técnico de Factus (ej: 1 para IVA)

    private String code; // Código DIAN (ej: "01" para IVA)

    private String name; // Nombre del impuesto (ej: "IVA", "INC")

    private String description; // Descripción opcional

    public Tributo() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
