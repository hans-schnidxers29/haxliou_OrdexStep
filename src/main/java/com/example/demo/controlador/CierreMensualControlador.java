package com.example.demo.controlador;

import com.example.demo.ModuloVentas.VentaServicio;
import com.example.demo.entidad.CierreMensual;
import com.example.demo.servicio.CierreMensualServicio;
import com.example.demo.servicio.ClienteService;
import com.example.demo.servicio.ProductoServicio;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cierre")
public class CierreMensualControlador {

    @Autowired
    private CierreMensualServicio servicio;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio servicioVentas;

    @GetMapping("/resumen")
    public String tuMes(@RequestParam(required = false) Integer mes,
                        @RequestParam(required = false) Integer anio,
                        Model model) {
        if (mes == null) mes = LocalDate.now().getMonthValue();
        if (anio == null) anio = LocalDate.now().getYear();

        Map<String, Object> datos = servicio.obtenerResumenProyectado(mes, anio);
        model.addAttribute("datos", datos);
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        model.addAttribute("montosClientes",clienteService.CantidadPedidosPorPersonas().get("cantidades"));
        model.addAttribute("nombresClientes",clienteService.CantidadPedidosPorPersonas().get("nombres"));
        model.addAttribute("productosNombres", productoServicio.NombreProductosVentas());
        model.addAttribute("productosCantidades", productoServicio.CantidadProductosVentas());
        List<String> etiquetas = servicioVentas.ListaMeses();
        List<BigDecimal> valores = servicioVentas.listarTotalVentas();
        model.addAttribute("labelsGrafica", etiquetas);
        model.addAttribute("datosGrafica", valores );
        return "viewCierre/resumenDeMes";
    }

    @GetMapping("/pdf/exportar")
    public void exportarPDF( HttpServletResponse response,@RequestParam Integer mes,
                            @RequestParam Integer anio) throws Exception {
        CierreMensual cierre = servicio.procesarCierreMes(mes,anio);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Cierre_" + cierre.getMes() + "_" + cierre.getAnio() + ".pdf");

        PdfWriter writer = new PdfWriter(response.getOutputStream());
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("REPORTE DE CIERRE MENSUAL").setBold().setFontSize(18));
        document.add(new Paragraph("Periodo: " + cierre.getNombreMes() + " " + cierre.getAnio()));
        document.add(new Paragraph("Fecha de Generación: " + cierre.getFechaCierre()));
        document.add(new Paragraph("--------------------------------------------------"));

        // --- SECCIÓN 1: FLUJO DE CAJA (MÉTODOS DE PAGO) ---
        document.add(new Paragraph("1. DESGLOSE DE INGRESOS POR MÉTODOS DE PAGO").setBold());
        Table tablePagos = new Table(2).useAllAvailableWidth();
        tablePagos.addCell("Ventas Efectivo:");
        tablePagos.addCell("$ " + cierre.getTotalVentasEfectivo().setScale(2, RoundingMode.HALF_UP));
        tablePagos.addCell("Ventas Tarjeta:");
        tablePagos.addCell("$ " + cierre.getTotalVentasTarjeta().setScale(2, RoundingMode.HALF_UP));
        tablePagos.addCell("Ventas Transferencia:");
        tablePagos.addCell("$ " + cierre.getTotalVentasTransferencia().setScale(2, RoundingMode.HALF_UP));
        tablePagos.addCell("Total Recaudado En Ventas al Mayor:");
        tablePagos.addCell("$" + cierre.getTotalVentasAlMayor().setScale(2,RoundingMode.HALF_UP));
        tablePagos.addCell("Recaudación Total (Bruta + Impuestos):");
        tablePagos.addCell("$ " + cierre.getRecaudacionTotal().setScale(2, RoundingMode.HALF_UP));
        document.add(tablePagos);

        // --- SECCIÓN 2: RESUMEN FINANCIERO ---
        document.add(new Paragraph("\n2. RESUMEN FINANCIERO Y UTILIDADES").setBold());
        Table tableFinanzas = new Table(2).useAllAvailableWidth();
        tableFinanzas.addCell("Ventas Netas (Sin Impuestos):");
        tableFinanzas.addCell("$ " + cierre.getRecaudacionBruta().setScale(2, RoundingMode.HALF_UP));
        tableFinanzas.addCell("Total Impuestos Recaudados (IVA):");
        tableFinanzas.addCell("$ " + cierre.getTotalImpuestos().setScale(2, RoundingMode.HALF_UP));
        tableFinanzas.addCell("Costo de Ventas (Mercancía Vendida):");
        tableFinanzas.addCell("$ " + (cierre.getRecaudacionBruta().subtract(cierre.getUtilidadBruta())).setScale(2, RoundingMode.HALF_UP));
        tableFinanzas.addCell("Utilidad Bruta:");
        tableFinanzas.addCell("$ " + cierre.getUtilidadBruta().setScale(2, RoundingMode.HALF_UP));
        tableFinanzas.addCell("Gastos Operativos (Egresos):");
        tableFinanzas.addCell("$ " + cierre.getTotalEgresos().setScale(2, RoundingMode.HALF_UP));
        tableFinanzas.addCell("Utilidad Neta del Ejercicio:");
        tableFinanzas.addCell("$ " + cierre.getUtilidadNeta().setScale(2, RoundingMode.HALF_UP));
        document.add(tableFinanzas);

        // --- SECCIÓN 3: ESTADÍSTICAS E INVENTARIO ---
        document.add(new Paragraph("\n3. ESTADÍSTICAS E INVENTARIO").setBold());
        Table tableStats = new Table(2).useAllAvailableWidth();
        tableStats.addCell("Cantidad de Pedidos:");
        tableStats.addCell(String.valueOf(cierre.getCantidadPedidos()));
        tableStats.addCell("Nuevos Clientes:");
        tableStats.addCell(String.valueOf(cierre.getNuevosClientes()));
        tableStats.addCell("Productos en Stock (SKUs):");
        tableStats.addCell(String.valueOf(cierre.getTotalProductosEnStock()));
        tableStats.addCell("Inversión Total en Compras Mes:");
        tableStats.addCell("$ " + cierre.getTotalCompras().setScale(2, RoundingMode.HALF_UP));
        tableStats.addCell("Valor Total Inventario Actual:");
        tableStats.addCell("$ " + cierre.getValorInventarioTotal().setScale(2, RoundingMode.HALF_UP));
        document.add(tableStats);

        document.close();
    }

    @GetMapping("/excel/exportar")
    public void exportarExce(HttpServletResponse response, @RequestParam Integer mes,
                              @RequestParam Integer anio) throws Exception {

        CierreMensual cierre = servicio.procesarCierreMes(mes, anio);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Cierre_" + cierre.getMes() + "_" + cierre.getAnio() + ".xlsx");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Cierre Mensual " + cierre.getMes());

        String[] columnas = {"Categoría", "Concepto", "Valor"};
        Row headerRow = sheet.createRow(0);
        for(int i=0; i<columnas.length; i++) {
            headerRow.createCell(i).setCellValue(columnas[i]);
        }

        Object[][] datos = {
                {"General", "Mes/Año", cierre.getNombreMes() + " " + cierre.getAnio()},
                {"General", "Fecha Cierre", cierre.getFechaCierre().toString()},
                {"Ingresos", "Ventas Efectivo", cierre.getTotalVentasEfectivo().doubleValue()},
                {"Ingresos", "Ventas Tarjeta", cierre.getTotalVentasTarjeta().doubleValue()},
                {"Ingresos", "Ventas Transferencia", cierre.getTotalVentasTransferencia().doubleValue()},
                {"Ingresos Ventas Al Mayor", cierre.getTotalVentasAlMayor().setScale(2,RoundingMode.HALF_UP)},
                {"Ingresos", "Recaudación Total (Con IVA)", cierre.getRecaudacionTotal().doubleValue()},
                {"Ingresos", "Ventas Netas (Sin IVA)", cierre.getRecaudacionBruta().doubleValue()},
                {"Impuestos", "Total Impuestos (IVA)", cierre.getTotalImpuestos().doubleValue()},
                {"Costos/Gastos", "Costo Mercancía Vendida", cierre.getRecaudacionBruta().subtract(cierre.getUtilidadBruta()).doubleValue()},
                {"Costos/Gastos", "Total Compras Mes", cierre.getTotalCompras().doubleValue()},
                {"Costos/Gastos", "Gastos Operativos (Egresos)", cierre.getTotalEgresos().doubleValue()},
                {"Utilidades", "Utilidad Bruta", cierre.getUtilidadBruta().doubleValue()},
                {"Utilidades", "Utilidad Neta", cierre.getUtilidadNeta().doubleValue()},
                {"Estadísticas", "Cantidad Pedidos", cierre.getCantidadPedidos()},
                {"Estadísticas", "Nuevos Clientes", cierre.getNuevosClientes()},
                {"Inventario", "Productos en Stock", cierre.getTotalProductosEnStock()},
                {"Inventario", "Valor Total Inventario", cierre.getValorInventarioTotal().doubleValue()}
        };

        int rowNum = 1;
        for (Object[] dato : datos) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue((String) dato[0]);
            row.createCell(1).setCellValue((String) dato[1]);
            if (dato[2] instanceof Double) {
                row.createCell(2).setCellValue((Double) dato[2]);
            } else if (dato[2] instanceof Integer) {
                row.createCell(2).setCellValue((Integer) dato[2]);
            } else {
                row.createCell(2).setCellValue((String) dato[2]);
            }
        }
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}
