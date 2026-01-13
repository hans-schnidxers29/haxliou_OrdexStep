package com.example.demo.controlador;

import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Caja;
import com.example.demo.pdf.PdfServicio;
import com.example.demo.servicio.CajaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/caja")
public class CajaControlador {

    @Autowired
    private CajaServicio servicio;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @Autowired
    private PdfServicio pdfService;

    /* =========================
       ABRIR CAJA
       ========================= */
    @PostMapping("/abrir")
    public String abrirCaja(@AuthenticationPrincipal UserDetails userDetails,
                            @RequestParam("montoInicial") BigDecimal montoInicial,
                            RedirectAttributes redirectAttributes) {

        try {
            Usuario usuario = servicioUsuario.findByEmail(userDetails.getUsername());
            servicio.EjecutarCaja(usuario, montoInicial);

            redirectAttributes.addFlashAttribute("success", "Caja abierta correctamente");
            return "redirect:/ventas/crear";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al abrir la caja: " + e.getMessage());
            return "redirect:/ventas/crear";
        }
    }

    /* =========================
       CERRAR CAJA
       ========================= */
    @PostMapping("/cerrar")
    public String cerrarCaja(@RequestParam("id") Long id,
                             @RequestParam("montoReal") BigDecimal montoReal,
                             RedirectAttributes redirectAttributes) {

        try {
            servicio.CerrarCaja(id, montoReal);
            redirectAttributes.addFlashAttribute("success", "Caja cerrada correctamente");
            return "redirect:/ventas/crear";

        } catch (DataAccessException e) {
            redirectAttributes.addFlashAttribute("error", "Error al cerrar la caja: " + e.getMessage());
            return "redirect:/ventas/crear";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al cerrar la caja: " + e.getMessage());
            return "redirect:/ventas/crear";
        }
    }

    /* =========================
       PDF CIERRE DE CAJA
       ========================= */
    @GetMapping("/cerrar/ticket/{id}")
    public ResponseEntity<byte[]> descargarPDF(@PathVariable Long id) throws Exception {

        Caja caja = servicio.cajaByid(id);
        Usuario usuario = caja.getUsuario();

        Map<String, Object> data = new HashMap<>();
        data.put("caja", caja);
        data.put("usuario", usuario);
        data.put("Base", caja.getMontoInicial());
        data.put("totalEgresos", caja.getEgresosTotales());
        data.put("totalGastos", caja.getGastosTotales());
        data.put("totalIngresos", caja.getIngresoTotal());
        data.put("diferencia", caja.getDiferencia());
        data.put("montoReal", caja.getMontoReal());

        byte[] pdf = pdfService.generarPdf("pdf/ticketCaja", data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ticket_Cierre_Caja_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /* =========================
       PDF REPORTE DE AVANCE
       ========================= */
    @GetMapping("/reporte-avance/{id}")
    public ResponseEntity<byte[]> reporteAvance(@PathVariable Long id) throws Exception {

        Caja cajaResumen = servicio.obtenerResumenActual(id);

        Map<String, Object> data = new HashMap<>();
        data.put("caja", cajaResumen);
        data.put("titulo", "Reporte de Avance de Caja");
        data.put("esAvance", true);

        byte[] pdf = pdfService.generarPdf("pdf/ticketCaja", data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=avance_caja_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
