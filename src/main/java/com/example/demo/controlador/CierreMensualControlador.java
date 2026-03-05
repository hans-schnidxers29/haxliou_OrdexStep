package com.example.demo.controlador;

import com.example.demo.Seguridad.SecurityService;
import com.example.demo.Seguridad.ServiceEmail;
import com.example.demo.entidad.Egresos;
import com.example.demo.entidad.Enum.TipoEgreso;
import com.example.demo.servicio.*;
import com.example.demo.entidad.CierreMensual;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/cierre")
public class  CierreMensualControlador {

    @Autowired
    private CierreMensualServicio servicio;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoServicio productoServicio;

    @Autowired
    private VentaServicio servicioVentas;

    @Autowired
    private CompraServicio compraServicio;

    @Autowired
    private EgresoServicio egresoServicio;

    @Autowired
    private ServiceEmail emailService;

    @Autowired
    private SecurityService securityService;

    @GetMapping("/resumen")
    public String tuMes(@RequestParam(required = false) Integer mes,
                        @RequestParam(required = false) Integer anio,
                        Model model) {
        if (mes == null) mes = LocalDate.now().getMonthValue();
        if (anio == null) anio = LocalDate.now().getYear();

        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);

        Map<String, Object> datos = servicio.obtenerResumenProyectado(mes, anio);
        model.addAttribute("datos", datos);
        model.addAttribute("mes", mes);
        model.addAttribute("anio", anio);
        model.addAttribute("montosClientes",clienteService.CantidadPedidosPorPersonas().get("cantidades"));
        model.addAttribute("nombresClientes",clienteService.CantidadPedidosPorPersonas().get("nombres"));
        model.addAttribute("productosNombres", productoServicio.NombreProductosVentas(inicio,fin));
        model.addAttribute("productosCantidades", productoServicio.CantidadProductosVentas(inicio,fin));
        List<String> etiquetas = servicioVentas.ListaMeses();
        List<BigDecimal> valores = servicioVentas.listarTotalVentas();
        model.addAttribute("labelsGrafica", etiquetas);
        model.addAttribute("datosGrafica", valores );
        return "viewCierre/resumenDeMes";
    }

    @GetMapping("/pdf/exportar")
    public void exportarPDF(HttpServletResponse response, @RequestParam Integer mes, @RequestParam Integer anio) throws Exception {

        // 1. Fechas y Contexto
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);
        Long empresaId = securityService.obtenerEmpresaId();

        // 2. Obtención de Datos
        CierreMensual cierre = servicio.procesarCierreMes(mes, anio);
        Map<String, Object> datosStock = compraServicio.StokMensual(inicio, fin);
        Map<String, Object> datosEgresos = egresoServicio.DatosEgresos(inicio, fin);
        List<Egresos> listaEgresosDetalle = egresoServicio.listaEgresosMensuales(inicio, fin);

        Map<String, Object> clientesData = clienteService.CantidadPedidosPorPersonas();
        List<String> nombresClientes = (List<String>) clientesData.get("nombres");
        List<Long> comprasClientes = (List<Long>) clientesData.get("cantidades");

        List<String> nombresProd = productoServicio.NombreProductosVentas(inicio, fin);
        List<BigDecimal> cantProd = productoServicio.CantidadProductosVentas(inicio, fin);

        // 3. Formateadores
        DecimalFormat df = new DecimalFormat("$ #,##0.00");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        // Lógica Financiera
        BigDecimal totalIngresos = cierre.getRecaudacionBruta() != null ? cierre.getRecaudacionBruta() : BigDecimal.ZERO;
        BigDecimal totalCompras = (BigDecimal) datosStock.getOrDefault("TotalEgresos", BigDecimal.ZERO);
        BigDecimal gastosFijos = (BigDecimal) datosEgresos.getOrDefault("GastosFijos", BigDecimal.ZERO);
        BigDecimal gastosVariables = (BigDecimal) datosEgresos.getOrDefault("GastosVariables", BigDecimal.ZERO);
        BigDecimal totalSalidas = totalCompras.add(gastosFijos).add(gastosVariables);
        BigDecimal residual = totalIngresos.subtract(totalSalidas);

        // 4. Generación del PDF en Memoria
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // --- HOJA 1: RESUMEN EJECUTIVO ---
        document.add(new Paragraph("REPORTE GERENCIAL - FLUJO DE CAJA").setBold().setFontSize(16));
        document.add(new Paragraph("Periodo: Mes " + mes + " del " + anio));
        document.add(new Paragraph("--------------------------------------------------"));

        // Sección Ingresos
        document.add(new Paragraph("\n1. TOTAL INGRESOS (VENTAS)").setBold().setFontSize(12).setFontColor(ColorConstants.BLUE));
        Table tableIngresos = new Table(2).useAllAvailableWidth();
        tableIngresos.addCell("Ventas Efectivo:");
        tableIngresos.addCell(df.format(cierre.getTotalVentasEfectivo()));
        tableIngresos.addCell("Ventas Tarjeta / Transf:");
        tableIngresos.addCell(df.format(cierre.getTotalVentasTarjeta().add(cierre.getTotalVentasTransferencia())));
        tableIngresos.addCell("Ventas Mayoristas:");
        tableIngresos.addCell(df.format(cierre.getTotalVentasAlMayor()));
        tableIngresos.addCell(new Cell().add(new Paragraph("TOTAL INGRESADO:").setBold()));
        tableIngresos.addCell(new Cell().add(new Paragraph(df.format(totalIngresos)).setBold()));
        document.add(tableIngresos);

        // Sección Gastos Resumidos
        document.add(new Paragraph("\n2. RESUMEN DE GASTOS Y COMPRAS").setBold().setFontSize(12));
        Table tableGastos = new Table(2).useAllAvailableWidth();
        tableGastos.addCell("Inversión en Mercancía (Compras):");
        tableGastos.addCell(df.format(totalCompras));
        tableGastos.addCell("Gastos Fijos:");
        tableGastos.addCell(df.format(gastosFijos));
        tableGastos.addCell("Gastos Variables:");
        tableGastos.addCell(df.format(gastosVariables));
        tableGastos.addCell(new Cell().add(new Paragraph("TOTAL SALIDAS:").setBold()));
        tableGastos.addCell(new Cell().add(new Paragraph(df.format(totalSalidas)).setBold().setFontColor(ColorConstants.RED)));
        document.add(tableGastos);

        // Sección Residual
        document.add(new Paragraph("\n3. RESIDUAL DEL MES").setBold().setFontSize(14));
        Table tableResidual = new Table(2).useAllAvailableWidth();
        tableResidual.addCell(new Cell().add(new Paragraph("CAJA LIBRE (RESIDUAL):").setBold()));
        Cell resCellValue = new Cell().add(new Paragraph(df.format(residual)).setBold().setFontSize(14));
        resCellValue.setFontColor(residual.signum() < 0 ? ColorConstants.RED : new DeviceRgb(40, 167, 69));
        tableResidual.addCell(resCellValue);
        document.add(tableResidual);

        // Sección Rendimiento
        document.add(new Paragraph("\n4. RENDIMIENTO COMERCIAL").setBold().setFontSize(12));
        if (nombresClientes != null && !nombresClientes.isEmpty()) {
            // Corregido: Quité df.format para cantidades de pedidos (Long)
            document.add(new Paragraph("Cliente Estrella: " + nombresClientes.get(0) + " (" + comprasClientes.get(0) + " pedidos)").setItalic());
        }

        document.add(new Paragraph("\nTop Productos Más Vendidos:").setUnderline());
        if (nombresProd != null && !nombresProd.isEmpty()) {
            int limit = Math.min(5, nombresProd.size());
            for (int i = 0; i < limit; i++) {
                document.add(new Paragraph((i + 1) + ". " + nombresProd.get(i) + " - " + cantProd.get(i) + " unidades"));
            }
        }

        // --- HOJA 2: ANEXO DE EGRESOS ---
        document.add(new AreaBreak());
        document.add(new Paragraph("5. ANEXO: DETALLE DE EGRESOS REGISTRADOS").setBold().setFontSize(12));
        document.add(new Paragraph("Gastos agrupados por categoría").setFontSize(10).setItalic());
        document.add(new Paragraph("\n"));

        Map<TipoEgreso, List<Egresos>> gastosAgrupados = listaEgresosDetalle.stream()
                .collect(Collectors.groupingBy(Egresos::getTipoEgreso));

        for (Map.Entry<TipoEgreso, List<Egresos>> entrada : gastosAgrupados.entrySet()) {
            document.add(new Paragraph("Categoría: " + entrada.getKey().name()).setBold().setFontColor(ColorConstants.DARK_GRAY));
            Table tableG = new Table(new float[]{2, 5, 3}).useAllAvailableWidth().setMarginBottom(15);
            tableG.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY)));
            tableG.addHeaderCell(new Cell().add(new Paragraph("Descripción").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY)));
            tableG.addHeaderCell(new Cell().add(new Paragraph("Monto").setBold().setBackgroundColor(ColorConstants.LIGHT_GRAY)));

            BigDecimal subtotalCategoria = BigDecimal.ZERO;
            for (Egresos e : entrada.getValue()) {
                tableG.addCell(new Cell().add(new Paragraph(e.getFechaRegistro().format(dtf)).setFontSize(9)));
                tableG.addCell(new Cell().add(new Paragraph(e.getDescripcion()).setFontSize(9)));
                tableG.addCell(new Cell().add(new Paragraph(df.format(e.getMonto())).setFontSize(9)));
                subtotalCategoria = subtotalCategoria.add(e.getMonto());
            }
            tableG.addCell(new Cell(1, 2).add(new Paragraph("Subtotal " + entrada.getKey().name()).setBold().setTextAlignment(TextAlignment.RIGHT)));
            tableG.addCell(new Cell().add(new Paragraph(df.format(subtotalCategoria)).setBold()));
            document.add(tableG);
        }

        document.close();

        // 5. Envío y Descarga
        byte[] pdfBytes = baos.toByteArray();
        String email = securityService.obtenerEmailUsuario();

        // Enviar email
        emailService.enviarReporteMensual(email, mes.toString(), anio.toString(), pdfBytes);

        // Configurar respuesta para descarga
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Gerencial_" + mes + "_" + anio + ".pdf");
        response.setContentLength(pdfBytes.length);
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }

    @GetMapping("/excel/exportar")
    public void exportarExce(HttpServletResponse response, @RequestParam Integer mes,
                             @RequestParam Integer anio) throws Exception {

        // 1. Obtención de Datos
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1).minusSeconds(1);

        CierreMensual cierre = servicio.procesarCierreMes(mes, anio);
        Map<String, Object> datosStock = compraServicio.StokMensual(inicio, fin);
        Map<String, Object> datosEgresos = egresoServicio.DatosEgresos(inicio, fin);
        List<Egresos> listaEgresosDetalle = egresoServicio.listaEgresosMensuales(inicio, fin);

        BigDecimal totalIngresos = cierre.getRecaudacionBruta() != null ? cierre.getRecaudacionBruta() : BigDecimal.ZERO;
        BigDecimal totalCompras = (BigDecimal) datosStock.getOrDefault("TotalEgresos", BigDecimal.ZERO);
        BigDecimal gastosFijos = (BigDecimal) datosEgresos.getOrDefault("GastosFijos", BigDecimal.ZERO);
        BigDecimal gastosVariables = (BigDecimal) datosEgresos.getOrDefault("GastosVariables", BigDecimal.ZERO);
        BigDecimal totalSalidas = totalCompras.add(gastosFijos).add(gastosVariables);
        BigDecimal residual = totalIngresos.subtract(totalSalidas);

        // 2. Crear Libro de Excel usando rutas completas para evitar conflictos con iText
        org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();

        // --- ESTILOS ---
        org.apache.poi.ss.usermodel.CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("$ #,##0.00"));

        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font hFont = workbook.createFont();
        hFont.setBold(true);
        hFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
        headerStyle.setFont(hFont);
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

        org.apache.poi.ss.usermodel.CellStyle boldStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font bFont = workbook.createFont();
        bFont.setBold(true);
        boldStyle.setFont(bFont);
        boldStyle.setDataFormat(workbook.createDataFormat().getFormat("$ #,##0.00"));

        // ==========================================
        // HOJA 1: RESUMEN
        // ==========================================
        org.apache.poi.ss.usermodel.Sheet sheet1 = workbook.createSheet("Resumen Ejecutivo");
        int r = 0;

        sheet1.createRow(r++).createCell(0).setCellValue("REPORTE GERENCIAL - FLUJO DE CAJA");
        sheet1.createRow(r++).createCell(0).setCellValue("Periodo: " + mes + "/" + anio);
        r++;

        // 1. INGRESOS
        org.apache.poi.ss.usermodel.Row rowI = sheet1.createRow(r++);
        org.apache.poi.ss.usermodel.Cell cellI = rowI.createCell(0);
        cellI.setCellValue("1. INGRESOS");
        cellI.setCellStyle(headerStyle);

        crearFilaExcel(sheet1, r++, "Ventas Efectivo", cierre.getTotalVentasEfectivo(), currencyStyle);
        crearFilaExcel(sheet1, r++, "Ventas Tarjeta / Transf", cierre.getTotalVentasTarjeta().add(cierre.getTotalVentasTransferencia()), currencyStyle);
        crearFilaExcel(sheet1, r++, "Ventas Mayoristas", cierre.getTotalVentasAlMayor(), currencyStyle);
        crearFilaExcel(sheet1, r++, "TOTAL INGRESOS", totalIngresos, boldStyle);
        r++;

        // 2. SALIDAS
        org.apache.poi.ss.usermodel.Row rowE = sheet1.createRow(r++);
        org.apache.poi.ss.usermodel.Cell cellE = rowE.createCell(0);
        cellE.setCellValue("2. SALIDAS Y COMPRAS");
        cellE.setCellStyle(headerStyle);

        crearFilaExcel(sheet1, r++, "Compras Mercancía", totalCompras, currencyStyle);
        crearFilaExcel(sheet1, r++, "Gastos Fijos", gastosFijos, currencyStyle);
        crearFilaExcel(sheet1, r++, "Gastos Variables", gastosVariables, currencyStyle);
        crearFilaExcel(sheet1, r++, "TOTAL SALIDAS", totalSalidas, boldStyle);
        r++;

        // RESIDUAL
        org.apache.poi.ss.usermodel.Row rowRes = sheet1.createRow(r++);
        rowRes.createCell(1).setCellValue("RESIDUAL DEL MES:");
        org.apache.poi.ss.usermodel.Cell cRes = rowRes.createCell(2);
        cRes.setCellValue(residual.doubleValue());
        cRes.setCellStyle(boldStyle);

        sheet1.autoSizeColumn(1);
        sheet1.autoSizeColumn(2);

        // ==========================================
        // HOJA 2: ANEXO DE EGRESOS (Nueva Hoja)
        // ==========================================
        org.apache.poi.ss.usermodel.Sheet sheet2 = workbook.createSheet("Anexo Detalle Egresos");
        int r2 = 0;

        org.apache.poi.ss.usermodel.Row titleAnexo = sheet2.createRow(r2++);
        titleAnexo.createCell(0).setCellValue("DETALLE DE EGRESOS POR CATEGORÍA");
        r2++;

        Map<TipoEgreso, List<Egresos>> agrupados = listaEgresosDetalle.stream()
                .collect(Collectors.groupingBy(Egresos::getTipoEgreso));

        for (Map.Entry<TipoEgreso, List<Egresos>> entrada : agrupados.entrySet()) {
            org.apache.poi.ss.usermodel.Row catRow = sheet2.createRow(r2++);
            org.apache.poi.ss.usermodel.Cell catCell = catRow.createCell(0);
            catCell.setCellValue("Categoría: " + entrada.getKey().name());
            catCell.setCellStyle(headerStyle);

            for (Egresos e : entrada.getValue()) {
                org.apache.poi.ss.usermodel.Row dRow = sheet2.createRow(r2++);
                dRow.createCell(0).setCellValue(e.getFechaRegistro().toLocalDate().toString());
                dRow.createCell(1).setCellValue(e.getDescripcion());
                org.apache.poi.ss.usermodel.Cell mCell = dRow.createCell(2);
                mCell.setCellValue(e.getMonto().doubleValue());
                mCell.setCellStyle(currencyStyle);
            }
            r2++; // Espacio entre categorías
        }

        sheet2.autoSizeColumn(0); sheet2.autoSizeColumn(1); sheet2.autoSizeColumn(2);

        // 4. Respuesta HTTP
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=Reporte_Financiero_" + mes + "_" + anio + ".xlsx");

        workbook.write(response.getOutputStream());
        workbook.close();
    }


    // MÉTODO AUXILIAR CORREGIDO PARA EVITAR CONFLICTOS
    private void crearFilaExcel(org.apache.poi.ss.usermodel.Sheet s, int idx, String txt, BigDecimal val, org.apache.poi.ss.usermodel.CellStyle style) {
        org.apache.poi.ss.usermodel.Row row = s.createRow(idx);
        row.createCell(1).setCellValue(txt);
        org.apache.poi.ss.usermodel.Cell c = row.createCell(2);
        c.setCellValue(val != null ? val.doubleValue() : 0.0);
        c.setCellStyle(style);
    }
}
