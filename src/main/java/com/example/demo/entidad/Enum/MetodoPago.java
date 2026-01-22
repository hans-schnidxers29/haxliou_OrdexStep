package com.example.demo.entidad.Enum;

public enum MetodoPago {

    TARJETA("Tarjeta"),
    MIXTO("Mixto"),
    EFECTIVO("Efectivo"),
    TRANFERENCIA("Transferencia");


    private final String descripcion;

    MetodoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
