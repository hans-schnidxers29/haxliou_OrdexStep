package com.example.demo.entidad.Enum;

public enum EstadoDeCaja {
    CERRADA("Cerrada"),
    EN_PROCESO("ABIERTA");

    private final String descripcion;

    EstadoDeCaja(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
