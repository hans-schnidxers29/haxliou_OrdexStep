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
import org.springframework.ui.Model;
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

    private PdfServicio pdfService;

    public CajaControlador(PdfServicio pdfService) {
        this.pdfService = pdfService;
    }

    @PostMapping("/abrir")
    public String AbrirCaja(@AuthenticationPrincipal UserDetails userDetails,
                            @ModelAttribute("caja") Caja caja,
                            RedirectAttributes redirectAttributes){
        try {
            Usuario usuario = servicioUsuario.findByEmail(userDetails.getUsername());

            // Verificar si ya tiene una caja abierta
            Caja cajaExistente = servicio.CajaAbierta(usuario);
            if (cajaExistente != null) {
                redirectAttributes.addFlashAttribute("error",
                        "Ya tienes una caja abierta. Debes cerrarla antes de abrir una nueva.");
                return "redirect:/ventas/crear";
            }

            BigDecimal MontoInicial = caja.getMontoInicial();

            // Validar que el monto inicial sea mayor a 0
            if (MontoInicial == null || MontoInicial.compareTo(BigDecimal.ZERO) <= 0) {
                redirectAttributes.addFlashAttribute("error",
                        "El monto inicial debe ser mayor a 0");
                return "redirect:/ventas/crear";
            }

            caja.setUsuario(usuario);
            servicio.EjecutarCaja(usuario, MontoInicial);
            redirectAttributes.addFlashAttribute("success", "Caja abierta correctamente");
            return "redirect:/ventas/crear";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Error al abrir la caja: " + e.getMessage());
            return "redirect:/ventas/crear";
        }
    }

    @PostMapping("/cerrar")
    public String CerrarCaja(@RequestParam("id") Long id,
                             @RequestParam("montoReal") BigDecimal montoReal,
                             @RequestParam(value = "observaciones", required = false) String observaciones,
                             RedirectAttributes redirectAttributes){
        try{
            servicio.CerrarCaja(id, montoReal);
            redirectAttributes.addFlashAttribute("success", "Caja cerrada correctamente");
            return "redirect:/ventas/crear";
        }catch(DataAccessException e){
            redirectAttributes.addFlashAttribute("error",
                    "Error al cerrar la caja: " + e.getMessage());
            return "redirect:/ventas/crear";
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error",
                    "Error al cerrar la caja: " + e.getMessage());
            return "redirect:/ventas/crear";
        }
    }

    @GetMapping("/cerrar/ticekc/{id}")
    public ResponseEntity<byte[]> descargarPDF(@PathVariable Long id) throws Exception{
        Caja caja = servicio.cajaByid(id);
        BigDecimal Base = caja.getMontoInicial();
        BigDecimal totalEgresos = caja.getEgresosTotales();
        BigDecimal totalGastos = caja.getGastosTotales();
        BigDecimal totalIngresos = caja.getIngresoTotal();
        BigDecimal diferencia = caja.getDiferencia();
        BigDecimal montoReal = caja.getMontoReal();

        Usuario usuario = servicioUsuario.findByEmail(caja.getUsuario().getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("caja", caja);
        data.put("usuario", usuario);
        data.put("Base", Base);
        data.put("totalEgresos", totalEgresos);
        data.put("totalGastos", totalGastos);
        data.put("totalIngresos", totalIngresos);
        data.put("diferencia", diferencia);
        data.put("montoReal", montoReal);

        byte[] pdf = pdfService.generarPdf("pdf/tikecteCaja", data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=ticket_Cierre_Caja_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/reporte-avance/{id}")
    public ResponseEntity<byte[]> reporteAvance(@PathVariable Long id) throws Exception {
        Caja cajaResumen = servicio.obtenerResumenActual(id);

        Map<String, Object> data = new HashMap<>();
        data.put("caja", cajaResumen);
        data.put("titulo", "Reporte de Avance de Caja");
        data.put("esAvance", true);

        byte[] pdf = pdfService.generarPdf("pdf/tikecteCaja", data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=avance_caja_" + id + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}