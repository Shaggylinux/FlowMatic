package com.back.exportacion;

import com.back.shared.dto.CvDataDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CvService {

    public void generarCv(CvDataDTO cvData, HttpServletResponse response) throws IOException {
        XWPFDocument doc = new XWPFDocument();

        String nombre = cvData.nombre() != null ? cvData.nombre() : "";
        String apellido = cvData.apellido() != null ? cvData.apellido() : "";

        XWPFParagraph title = doc.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        title.setSpacingAfter(200);
        XWPFRun titleRun = title.createRun();
        titleRun.setText(nombre + " " + apellido);
        titleRun.setBold(true);
        titleRun.setFontSize(18);
        titleRun.setFontFamily("Calibri");
        titleRun.setColor("0D9488");

        addSection(doc, "Contacto");
        addLine(doc, "Email: " + (cvData.email() != null ? cvData.email() : "\u2014"));
        if (cvData.telefono() != null && !cvData.telefono().isBlank())
            addLine(doc, "Tel\u00e9fono: " + cvData.telefono());
        if (cvData.ciudad() != null && !cvData.ciudad().isBlank())
            addLine(doc, "Ubicaci\u00f3n: " + cvData.ciudad());

        addSection(doc, "Perfil Profesional");
        if (cvData.cargo() != null && !cvData.cargo().isBlank())
            addLine(doc, "Cargo deseado: " + cvData.cargo());
        addLine(doc, "Experiencia: " + cvData.experiencia() + " a\u00f1os");

        if (cvData.tecnologias() != null && !cvData.tecnologias().isBlank()) {
            addSection(doc, "Habilidades T\u00e9cnicas");
            for (String tech : cvData.tecnologias().split(",")) {
                String t = tech.trim();
                if (!t.isEmpty()) addBullet(doc, t);
            }
        }

        if (cvData.idiomas() != null && !cvData.idiomas().isBlank()) {
            addSection(doc, "Idiomas");
            addLine(doc, cvData.idiomas());
        }

        if (cvData.disponibilidad() != null && !cvData.disponibilidad().isBlank()) {
            addSection(doc, "Disponibilidad");
            addLine(doc, cvData.disponibilidad());
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        String filename = "CV_" + nombre + "_" + apellido + ".docx";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        doc.write(response.getOutputStream());
        doc.close();
    }

    private void addSection(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(300);
        p.setSpacingAfter(80);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setBold(true);
        r.setFontSize(14);
        r.setFontFamily("Calibri");
        r.setColor("0D9488");
    }

    private void addLine(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setSpacingBefore(40);
        p.setSpacingAfter(40);
        XWPFRun r = p.createRun();
        r.setText(text);
        r.setFontSize(11);
        r.setFontFamily("Calibri");
        r.setColor("334155");
    }

    private void addBullet(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        p.setIndentationLeft(400);
        p.setSpacingBefore(30);
        p.setSpacingAfter(30);
        XWPFRun r = p.createRun();
        r.setText("\u2022  " + text);
        r.setFontSize(11);
        r.setFontFamily("Calibri");
        r.setColor("334155");
    }
}
