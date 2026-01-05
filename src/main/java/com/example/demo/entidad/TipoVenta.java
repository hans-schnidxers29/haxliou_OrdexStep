package com.example.demo.entidad;

public enum TipoVenta {
    UNIDAD("Unidad"),
    PESO("Peso (kg)"),
    VOLUMEN("Volumen (L)"),
    METRO("Metro");

    private final String descripcion;

    TipoVenta(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;

    }
}

