package com.example.demo.entidad.Enum;

public enum TipoEgreso {

     GASTO_FIJO("Fijo"),
     GASTOS_VARIABLES("Variables");

    private final String decripcion;

    TipoEgreso(String decripcion) {
        this.decripcion = decripcion;
    }

    public String getDecripcion() {
        return decripcion;
    }
}
