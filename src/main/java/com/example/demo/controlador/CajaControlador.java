package com.example.demo.controlador;

import com.example.demo.Login.Servicio.ServicioUsuario;
import com.example.demo.Login.Usuario;
import com.example.demo.entidad.Caja;
import com.example.demo.pdf.PdfServicio;
import com.example.demo.servicio.CajaServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/caja")
public class CajaControlador {

    @Autowired private CajaServicio servicio;


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


    @PostMapping("/cerrar/{id}")
    public String cerrarCaja(@PathVariable Long id, RedirectAttributes redirectAttributes,Model model) {

        model.addAttribute("cajaId", id);
        try {
            Caja caja = servicio.cajaByid(id);
            BigDecimal montoReal = caja.getMontoReal();

            servicio.CerrarCaja(id, montoReal);
            redirectAttributes.addFlashAttribute("success", "Caja cerrada correctamente");
            return "caja/cierre_exitoso";

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
        try {

            Caja caja1 = servicio.cajaByid(id);
            Usuario usuario = caja1.getUsuario();

            Map<String, Object> caja = servicio.obtenerResumenActual(id);
            caja.put("usuario", usuario);
            caja.put("titulo", "Reporte de Cierre de Caja");
            byte[] pdf = pdfService.generarPdf("pdf/ticketCaja", caja);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=ticket_Cierre_Caja_" + id + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }
/*
    *//* =========================
       PDF REPORTE DE AVANCE
       ========================= */
    @GetMapping("/reporte-cierre-final/{id}")
    public ResponseEntity<byte[]> cerrarCajaYGenerarPDF(@PathVariable Long id) throws Exception {

        // 1. Obtener y cerrar la caja (Persistencia)
        Caja cajaOriginal = servicio.cajaByid(id);
        BigDecimal montoReal = cajaOriginal.getMontoReal();
        Caja cajaCerrada = servicio.CerrarCaja(cajaOriginal.getId(), montoReal);

        Map<String, Object> resumen = servicio.obtenerResumenActual(id);

        // 3. Crear el mapa principal para el PDF y combinar
        Map<String, Object> data = new HashMap<>();
        data.putAll(resumen); // Inserta todos los valores del resumen automáticamente

        // 4. Agregar objetos adicionales necesarios para el ticket
        data.put("caja", cajaCerrada);
        data.put("usuario", cajaCerrada.getUsuario());
        data.put("titulo", "Reporte de Cierre Definitivo");
        data.put("montoRealContado", montoReal); // Valor físico reportado

        byte[] pdf = pdfService.generarPdf("pdf/ticketFinal", data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=cierre_final_caja_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
