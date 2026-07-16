package com.back.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import com.back.model.Usuario;
import com.back.model.Candidato;
import com.back.model.Evento;
import jakarta.servlet.http.HttpServletResponse;


@Service
public class ExcelService {
    public void exportarUsuarios(List<Usuario> usuarios, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Usuarios");

        Row row = sheet.createRow(0);
        String[] columnas = {"ID", "Email", "Rol", "Activo"};
        
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columnas[i]);
        }

        int rowIdx = 1;
        for (Usuario u : usuarios) {
            Row dataRow = sheet.createRow(rowIdx++);
            dataRow.createCell(0).setCellValue(u.getId());
            dataRow.createCell(1).setCellValue(u.getEmail());
            dataRow.createCell(2).setCellValue(u.getRol());
            dataRow.createCell(3).setCellValue(u.isActivo() ? "T" : "F");
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportarCandidatos(List<Candidato> candidatos, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Candidatos");

        String[] columnas = {"ID", "Nombre", "Apellido", "Email", "Teléfono", "Cargo", "Ciudad",
                             "Experiencia", "Disponibilidad", "Tecnologías", "Idiomas", "Estado", "Proceso"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            header.createCell(i).setCellValue(columnas[i]);
        }

        int rowIdx = 1;
        for (Candidato c : candidatos) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(c.getId());
            r.createCell(1).setCellValue(c.getUsername() != null ? c.getUsername() : "");
            r.createCell(2).setCellValue(c.getApellido() != null ? c.getApellido() : "");
            r.createCell(3).setCellValue("");
            r.createCell(4).setCellValue(c.getTelefono() != null ? c.getTelefono() : "");
            r.createCell(5).setCellValue(c.getCargo() != null ? c.getCargo() : "");
            r.createCell(6).setCellValue(c.getCiudad() != null ? c.getCiudad() : "");
            r.createCell(7).setCellValue(c.getExperiencia() != null ? c.getExperiencia() : 0);
            r.createCell(8).setCellValue(c.getDisponibilidad() != null ? c.getDisponibilidad() : "");
            r.createCell(9).setCellValue(c.getTecnologias() != null ? c.getTecnologias() : "");
            r.createCell(10).setCellValue(c.getIdiomas() != null ? c.getIdiomas() : "");
            r.createCell(11).setCellValue(c.getEstado() != null ? c.getEstado() : "Registrado");
            r.createCell(12).setCellValue(c.getProcesoActual() != null ? c.getProcesoActual() : "");
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportarEventos(List<Evento> eventos, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Entrevistas");

        String[] columnas = {"Fecha", "Hora", "Candidato", "Vacante", "Tipo", "Modalidad",
                             "Ubicaci\u00f3n", "Entrevistador", "Estado"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            header.createCell(i).setCellValue(columnas[i]);
        }

        int rowIdx = 1;
        for (Evento e : eventos) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(e.getFecha() != null ? e.getFecha().toString() : "");
            r.createCell(1).setCellValue(e.getHora() != null ? e.getHora().toString() : "");
            r.createCell(2).setCellValue(e.getCandidatoNombre() != null ? e.getCandidatoNombre() : "");
            r.createCell(3).setCellValue(e.getVacante() != null ? e.getVacante() : "");
            r.createCell(4).setCellValue(e.getTipo() != null ? e.getTipo() : "");
            r.createCell(5).setCellValue(e.getModalidad() != null ? e.getModalidad() : "");
            r.createCell(6).setCellValue(e.getLugar() != null ? e.getLugar() : "");
            r.createCell(7).setCellValue(e.getEntrevistador() != null ? e.getEntrevistador() : "");
            r.createCell(8).setCellValue(e.getEstado() != null ? e.getEstado() : "");
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}