package com.example.demo.entidad.Enum;


public enum EstadoPedido {

    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    EN_PREPARACION("En Preparación"),
    ENVIADO("Enviado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    private final String descripcion;

    EstadoPedido(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
