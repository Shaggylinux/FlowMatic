package com.back.service;

import com.back.model.Candidato;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CvService {

    public void generarCv(Candidato candidato, String email, HttpServletResponse response) throws IOException {
        XWPFDocument doc = new XWPFDocument();

        String nombre = candidato.getUsername() != null ? candidato.getUsername() : "";
        String apellido = candidato.getApellido() != null ? candidato.getApellido() : "";

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
        addLine(doc, "Email: " + (email != null ? email : "\u2014"));
        if (candidato.getTelefono() != null && !candidato.getTelefono().isBlank())
            addLine(doc, "Tel\u00e9fono: " + candidato.getTelefono());
        if (candidato.getCiudad() != null && !candidato.getCiudad().isBlank())
            addLine(doc, "Ubicaci\u00f3n: " + candidato.getCiudad());

        addSection(doc, "Perfil Profesional");
        if (candidato.getCargo() != null && !candidato.getCargo().isBlank())
            addLine(doc, "Cargo deseado: " + candidato.getCargo());
        int exp = candidato.getExperiencia() != null ? candidato.getExperiencia() : 0;
        addLine(doc, "Experiencia: " + exp + " a\u00f1os");

        if (candidato.getTecnologias() != null && !candidato.getTecnologias().isBlank()) {
            addSection(doc, "Habilidades T\u00e9cnicas");
            for (String tech : candidato.getTecnologias().split(",")) {
                String t = tech.trim();
                if (!t.isEmpty()) addBullet(doc, t);
            }
        }

        if (candidato.getIdiomas() != null && !candidato.getIdiomas().isBlank()) {
            addSection(doc, "Idiomas");
            addLine(doc, candidato.getIdiomas());
        }

        if (candidato.getDisponibilidad() != null && !candidato.getDisponibilidad().isBlank()) {
            addSection(doc, "Disponibilidad");
            addLine(doc, candidato.getDisponibilidad());
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
