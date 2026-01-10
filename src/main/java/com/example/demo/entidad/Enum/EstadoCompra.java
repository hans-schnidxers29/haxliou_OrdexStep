package com.example.demo.entidad.Enum;

public enum EstadoCompra {
    BORRADOR("Borrador"),
    CONFIRMADA("Confirmada"),
    ANULADAD("Anulada");

    private final String descripcion;

    EstadoCompra(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
