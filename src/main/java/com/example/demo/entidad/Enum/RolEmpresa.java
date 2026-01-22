package com.example.demo.entidad.Enum;

public enum RolEmpresa {
    PROPIETARIO("Propietario"),
    EMPLEADO("Empleado");

    private final String descripcion;

    public String getDescripcion() {
        return descripcion;
    }

    RolEmpresa(String descripcion) {
        this.descripcion = descripcion;
    }
}
