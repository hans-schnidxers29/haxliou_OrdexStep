package com.example.demo.pdf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfServicio {

    @Autowired
    private static SpringTemplateEngine templateEngine;

    public static byte[] generarPdf(String templateName, Map<String, Object> data) throws Exception {

        // Preparar Thymeleaf
        Context context = new Context();
        context.setVariables(data);

        // Renderizar HTML
        String html = templateEngine.process(templateName, context);

        // PDF
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(outputStream);

        return outputStream.toByteArray();
    }
}
