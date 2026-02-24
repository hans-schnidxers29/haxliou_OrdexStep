package com.example.demo.controlador;

import com.example.demo.entidad.Enum.MetodoPago;
import com.example.demo.servicio.ServicioUsuario;
import com.example.demo.entidad.Usuario;
import com.example.demo.entidad.Egresos;
import com.example.demo.servicio.EgresoServicio;
import org.hibernate.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequestMapping("/egresos")
public class EgresosControlador {


    @Autowired
    private EgresoServicio egresoServicio;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @GetMapping("/crear")
    public String Mostrarformulario(Model model){
        Egresos egresos = new Egresos();
        model.addAttribute("egreso",egresos);
        model.addAttribute("metodosPago", MetodoPago.values());
        return "viewEgresos/crearEgreso";
    }

    @PostMapping("/crear/nuevo")
    public String Crearegreso(@ModelAttribute("egreso") Egresos egresos, @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes, @RequestParam(defaultValue = "0") BigDecimal montoEfectivo,
                              @RequestParam(defaultValue = "0") BigDecimal montoTarjeta,
                              @RequestParam(defaultValue = "0") BigDecimal montoTransferencia){
        try {
            if (egresos.getMonto() == null) {
                redirectAttributes.addFlashAttribute("error", "El monto no puede ser vacio");
                return "redirect:/egresos/crear";
            }
            Usuario usuario_registra = servicioUsuario.findByEmail(userDetails.getUsername());
            if (usuario_registra == null) {
                redirectAttributes.addFlashAttribute("error", "Error al crear el egreso: Usuario no encontrado");
                return "redirect:/egresos/crear";
            }
            egresos.setUsuario(usuario_registra);
            if(egresos.getMetodoPago() == MetodoPago.MIXTO){

                BigDecimal montoTotal = montoEfectivo.add(montoTarjeta).add(montoTransferencia);

                if(montoTotal.compareTo(egresos.getMonto()) != 0){
                    throw new RuntimeException("La suma de los medios de pago ($" + montoTotal +
                            ") debe ser igual al total del egreso ($" + egresos.getMonto() + ")");
                }
                if (montoTotal.signum()<= 0){
                    throw new RuntimeException("el monto debe ser mayor a cero");
                }

                if (montoEfectivo.signum() > 0) {
                    egresos.addPago("EFECTIVO", montoEfectivo);
                }
                if (montoTarjeta.signum() > 0) {
                    egresos.addPago("TARJETA", montoTarjeta);
                }

                if (montoTransferencia.signum() > 0) {
                    egresos.addPago("TRANFERENCIA", montoTransferencia);
                }
            }else{
                if (egresos.getMonto().signum() <= 0) {
                    throw new RuntimeException("El monto del egreso debe ser mayor a cero");
                }
                egresos.addPago(egresos.getMetodoPago().name().toUpperCase(),egresos.getMonto());
            }
            if (egresos.isSalioCaja()) egresos.setSalioCaja(true);
            egresoServicio.CrearGasto(egresos);
            redirectAttributes.addFlashAttribute("success", "Egreso creado exitosamente");
            return "redirect:/egresos/listar";

        }catch (Exception e){
            redirectAttributes.addFlashAttribute("error", "Error al crear el egreso: " + e.getMessage());
            return "redirect:/egresos/listar";
        }
    }

    @GetMapping("/listar")
    public String ListaDeEgresos(Model model){
        model.addAttribute("egresos",egresoServicio.ListarGastos());
        int anio = LocalDate.now().getYear();
        int mes = LocalDate.now().getMonthValue();
        LocalDate primerDia = LocalDate.of(anio, mes, 1);
        LocalDateTime inicio = primerDia.atStartOfDay();
        LocalDateTime fin = primerDia.with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        model.addAttribute("datos",egresoServicio.DatosEgresos(inicio,fin));
        return "viewEgresos/index";
    }
    @GetMapping("eliminar/{id}")
    public String Eliminar(@PathVariable Long id, RedirectAttributes flash){
        try{
            egresoServicio.deleteGasto(id);
            flash.addFlashAttribute("success","eliminado Correctamente");
            return "redirect:/egresos/listar";
        }catch (Exception e){
            flash.addFlashAttribute("error", "Error al eliminar");
            return "redirect:/egresos/listar";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Egresos egreso = egresoServicio.ObtenerEgreso(id);
        model.addAttribute("egreso", egreso);
        model.addAttribute("metodosPago", MetodoPago.values());
        return "viewEgresos/editarEgreso";
    }
    @PostMapping("/editar/{id}")
    public String editarEgreso(@PathVariable Long id,
                               @ModelAttribute("egreso") Egresos egreso,
                               @RequestParam(defaultValue = "0") BigDecimal montoEfectivo,
                               @RequestParam(defaultValue = "0") BigDecimal montoTarjeta,
                               @RequestParam(defaultValue = "0") BigDecimal montoTransferencia,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "viewEgresos/editarEgreso";
        }

        try {
            // 1. Buscamos el egreso existente
            Egresos egresoExistente = egresoServicio.ObtenerEgreso(id);
            if (egresoExistente == null) {
                redirectAttributes.addFlashAttribute("error", "El egreso no existe.");
                return "redirect:/egresos/listar";
            }

            // 2. Validación básica de monto
            if (egreso.getMonto() == null || egreso.getMonto().signum() <= 0) {
                throw new RuntimeException("El monto debe ser mayor a cero");
            }

            // 3. Actualizamos campos básicos
            egresoExistente.setTipoEgreso(egreso.getTipoEgreso());
            egresoExistente.setMonto(egreso.getMonto());
            egresoExistente.setDescripcion(egreso.getDescripcion());
            egresoExistente.setMetodoPago(egreso.getMetodoPago());

            // 4. GESTIÓN DE PAGOS (Limpiamos los anteriores para re-procesar)
            // Nota: Asegúrate de tener orphanRemoval = true en la relación de pagos en la entidad Egresos
            egresoExistente.getPagos().clear();

            if (egresoExistente.getMetodoPago() == MetodoPago.MIXTO) {
                BigDecimal montoTotalCalculado = montoEfectivo.add(montoTarjeta).add(montoTransferencia);

                if (montoTotalCalculado.compareTo(egresoExistente.getMonto()) != 0) {
                    throw new RuntimeException("La suma de los medios ($" + montoTotalCalculado +
                            ") debe coincidir con el total ($" + egresoExistente.getMonto() + ")");
                }

                if (montoEfectivo.signum() > 0) egresoExistente.addPago("EFECTIVO", montoEfectivo);
                if (montoTarjeta.signum() > 0) egresoExistente.addPago("TARJETA", montoTarjeta);
                if (montoTransferencia.signum() > 0) egresoExistente.addPago("TRANSFERENCIA", montoTransferencia);

            } else {
                // Pago único
                egresoExistente.addPago(egresoExistente.getMetodoPago().name().toUpperCase(), egresoExistente.getMonto());
            }
            if(egreso.isSalioCaja()){
                egresoExistente.setSalioCaja(true);
            }else{
                egresoExistente.setSalioCaja(false);
            }

            // 5. Guardar cambios
            egresoServicio.UpdateEgreso(egresoExistente);
            redirectAttributes.addFlashAttribute("success", "Egreso #" + id + " actualizado correctamente.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar: " + e.getMessage());
            return "redirect:/egresos/listar";
        }

        return "redirect:/egresos/listar";
    }
}

