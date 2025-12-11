package com.example.demo.pdf;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfServicio {

    private final SpringTemplateEngine templateEngine;

    public PdfServicio(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public byte[] generarPdf(String templateName, Map<String, Object> data) throws Exception {

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