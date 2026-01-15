package com.example.demo.servicio;

import com.example.demo.ModuloVentas.VentaRepositorio;
import com.example.demo.entidad.CierreMensual;
import com.example.demo.entidad.DetalleCompra;
import com.example.demo.entidad.Productos;
import com.example.demo.repositorio.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CierreMensualImpTest {

    @Mock private CierreMensualRepositorio cierreRepo;
    @Mock private VentaRepositorio ventaRepo;
    @Mock private EgresoRepositorio egresoRepo;
    @Mock private ProductoRepositorio productoRepo;
    @Mock private ClienteRepositorio clienteRepo;
    @Mock private ComprasRepositorio comprasRepo;
    @Mock private PedidoRepositorio pedidoRepo;
    @Mock private DetalleCompraRepositorio detalleRepo;

    @InjectMocks
    private CierreMensualImp cierreServicio;

    private Productos productoPrueba;

    @BeforeEach
    void setUp() {
        productoPrueba = new Productos();
        productoPrueba.setId(1L);
        productoPrueba.setNombre("Producto Test");
        productoPrueba.setCantidad(new BigDecimal("10"));
    }

    @Test
    void testProcesarCierreMesExitoso() {
        // --- 1. CONFIGURACIÓN DE MOCKS (GIVEN) ---
        int mes = 1;
        int anio = 2024;

        // IMPORTANTE: Definir primero el comportamiento general y LUEGO el específico,
        // o usar eq() para todos para evitar que uno sobreescriba al otro.
        when(ventaRepo.sumaPorMetodoPago(any(), any(), anyString())).thenReturn(BigDecimal.ZERO);
        when(ventaRepo.sumaPorMetodoPago(any(), any(), eq("EFECTIVO"))).thenReturn(new BigDecimal("1000"));

        // Mock de impuestos y otros valores
        when(ventaRepo.sumaImpuestosMes(any(), any())).thenReturn(new BigDecimal("100"));
        when(pedidoRepo.sumaImpuestosPedidos(any(), any())).thenReturn(BigDecimal.ZERO);
        when(egresoRepo.sumarEgresosPorDia(any(), any())).thenReturn(new BigDecimal("200"));
        when(ventaRepo.sumaPorMes(any(), any())).thenReturn(new BigDecimal("400"));

        // Mock de Inventario
        when(productoRepo.findAll()).thenReturn(Arrays.asList(productoPrueba));

        DetalleCompra detalle = new DetalleCompra();
        detalle.setPrecioUnitario(new BigDecimal("35.00"));
        // Asegúrate que findTopByProductos... coincida con tu interfaz
        when(detalleRepo.findTopByProductosOrderByCompra_FechaCompraDesc(any(Productos.class))).thenReturn(Optional.of(detalle));

        when(clienteRepo.contarNuevosClientesPorRango(any(), any())).thenReturn(5);

        // Corregido: PedidoRepositorio devuelve Long según tu lógica de .intValue()
        when(pedidoRepo.cantidadPedidosPorRango(any(), any())).thenReturn(10L);

        // Simular el guardado final
        when(cierreRepo.save(any(CierreMensual.class))).thenAnswer(i -> i.getArguments()[0]);

        // --- 2. EJECUCIÓN (WHEN) ---
        CierreMensual resultado = cierreServicio.procesarCierreMes(mes, anio);

// --- AGREGA ESTO PARA VER LOS RESULTADOS ---
        System.out.println("======= RESULTADOS DEL CIERRE =======");
        System.out.println("Mes/Año: " + resultado.getMes() + "/" + resultado.getAnio());
        System.out.println("Recaudación Total: " + resultado.getRecaudacionTotal());
        System.out.println("Valor Inventario: " + resultado.getValorInventarioTotal());
        System.out.println("Utilidad Neta: " + resultado.getUtilidadNeta());
        System.out.println("=====================================");

// --- 3. VERIFICACIONES ---
// ... tus asserts existentes

        // --- 3. VERIFICACIONES (THEN) ---
        assertNotNull(resultado);
        assertEquals(mes, resultado.getMes());

        // Comparar usando compareTo para evitar problemas de escala (1000 vs 1000.00)
        assertTrue(new BigDecimal("1000").compareTo(resultado.getRecaudacionTotal()) == 0, "La recaudación total debería ser 1000");
        assertTrue(new BigDecimal("100").compareTo(resultado.getTotalImpuestos()) == 0, "Los impuestos deberían ser 100");
        assertTrue(new BigDecimal("900").compareTo(resultado.getRecaudacionBruta()) == 0, "La recaudación bruta debería ser 900");
        assertTrue(new BigDecimal("350.00").compareTo(resultado.getValorInventarioTotal()) == 0, "El valor de inventario debería ser 350");

        verify(cierreRepo, times(1)).save(any(CierreMensual.class));
        verify(cierreRepo, times(1)).eliminarCierreExistente(mes, anio);
    }
}