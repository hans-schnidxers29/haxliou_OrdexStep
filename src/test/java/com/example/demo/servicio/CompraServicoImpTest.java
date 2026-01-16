package com.example.demo.servicio;

import com.example.demo.repositorio.ComprasRepositorio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraServicoImpTest {

    @Mock
    private ComprasRepositorio repositorio; // Simulamos el repositorio

    @InjectMocks
    private CompraServicioImp comprasService; // La clase que tiene el método a probar

    @Test
    void debeGenerarReferenciaConFormatoCorrecto() {
        // 1. Preparación (Given): Cuando el repo devuelva el número 5
        when(repositorio.obtenerNumeroSigReferencia()).thenReturn(5L);

        // 2. Ejecución (When): Llamamos al método
        String resultado = comprasService.GenerarReferenciasDeCompras();

        // 3. Verificación (Then): El resultado debe ser COMP-000005
        assertEquals("COMP-000005", resultado);
        System.out.println(resultado);
    }

    @Test
    void debeRellenarConCerosCuandoElNumeroEsGrande() {
        // Probamos con un número de más dígitos
        when(repositorio.obtenerNumeroSigReferencia()).thenReturn(123L);

        String resultado = comprasService.GenerarReferenciasDeCompras();

        assertEquals("COMP-000123", resultado);
        System.out.println(resultado);
    }

}