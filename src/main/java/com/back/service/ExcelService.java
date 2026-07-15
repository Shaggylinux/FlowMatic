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
import com.back.model.Evento;
import jakarta.servlet.http.HttpServletResponse;


@Service
public class ExcelService {
    public void exportarUsuarios(List<Usuario> usuarios, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Usuarios");

        Row row = sheet.createRow(0);
        String[] columnas = {"ID", "Username", "Apellido", "Email", "Rol", "Activo", "Etapa"};
        
        for (int i = 0; i < columnas.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(columnas[i]);
        }

        int rowIdx = 1;
        for (Usuario u : usuarios) {
            Row dataRow = sheet.createRow(rowIdx++);
            dataRow.createCell(0).setCellValue(u.getId());
            dataRow.createCell(1).setCellValue(u.getUsername());
            dataRow.createCell(2).setCellValue(u.getApellido());
            dataRow.createCell(3).setCellValue(u.getEmail());
            dataRow.createCell(4).setCellValue(u.getRol());
            dataRow.createCell(5).setCellValue(u.isActivo() ? "T" : "F");
            
            if(u.getEstado() == null){
                dataRow.createCell(6).setCellValue("Registrado");
            } else {
                dataRow.createCell(6).setCellValue(u.getEstado());
            }
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportarCandidatos(List<Usuario> candidatos, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Candidatos");

        String[] columnas = {"ID", "Nombre", "Apellido", "Email", "Teléfono", "Cargo", "Ciudad",
                             "Experiencia", "Disponibilidad", "Tecnologías", "Idiomas", "Estado", "Proceso"};
        Row header = sheet.createRow(0);
        for (int i = 0; i < columnas.length; i++) {
            header.createCell(i).setCellValue(columnas[i]);
        }

        int rowIdx = 1;
        for (Usuario u : candidatos) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(u.getId());
            r.createCell(1).setCellValue(u.getUsername() != null ? u.getUsername() : "");
            r.createCell(2).setCellValue(u.getApellido() != null ? u.getApellido() : "");
            r.createCell(3).setCellValue(u.getEmail() != null ? u.getEmail() : "");
            r.createCell(4).setCellValue(u.getTelefono() != null ? u.getTelefono() : "");
            r.createCell(5).setCellValue(u.getCargo() != null ? u.getCargo() : "");
            r.createCell(6).setCellValue(u.getCiudad() != null ? u.getCiudad() : "");
            r.createCell(7).setCellValue(u.getExperiencia() != null ? u.getExperiencia() : 0);
            r.createCell(8).setCellValue(u.getDisponibilidad() != null ? u.getDisponibilidad() : "");
            r.createCell(9).setCellValue(u.getTecnologias() != null ? u.getTecnologias() : "");
            r.createCell(10).setCellValue(u.getIdiomas() != null ? u.getIdiomas() : "");
            r.createCell(11).setCellValue(u.getEstado() != null ? u.getEstado() : "Registrado");
            r.createCell(12).setCellValue(u.getProcesoActual() != null ? u.getProcesoActual() : "");
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