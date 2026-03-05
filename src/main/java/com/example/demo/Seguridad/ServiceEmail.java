package com.example.demo.Seguridad;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class ServiceEmail {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void enviarReporteMensual(String destinatario, String mes, String anio, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // "true" indica que es un mensaje multiparte (con adjuntos)
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(destinatario);
            helper.setSubject("Reporte Gerencial Disponible - " + mes + "/" + anio);
            helper.setText("Hola,\n\nSe adjunta el reporte de flujo de caja y residual del mes solicitado.\n\nSaludos.");

            // Adjuntamos el PDF desde el arreglo de bytes
            InputStreamSource attachment = new ByteArrayResource(pdfBytes);
            helper.addAttachment("Reporte_Gerencial_" + mes + "_" + anio + ".pdf", attachment);

            mailSender.send(message);
            System.out.println("Correo enviado con éxito a: " + destinatario);

        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage());
        }
    }

    @Async
    public void enviarCorreoConPlantilla(String destinatario, String asunto, Map<String, Object> modelos, byte[] pdfBytes, String nombrePdf) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 1. Procesar la plantilla HTML de Thymeleaf
            Context context = new Context();
            context.setVariables(modelos);
            String htmlContent = templateEngine.process("pdf/ticketFinal", context);

            // 2. Configurar el correo
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(htmlContent, true); // "true" activa el renderizado de HTML
            helper.setFrom("haxliou@gmail.com");

            // 3. Adjuntar el PDF
            if (pdfBytes != null) {
                helper.addAttachment(nombrePdf, new ByteArrayResource(pdfBytes));
            }

            mailSender.send(message);
            System.out.println("Correo enviado con éxito a: " + destinatario);
        } catch (MessagingException e) {
            throw new RuntimeException("Error al enviar el correo: " + e.getMessage());
        }
    }
}
