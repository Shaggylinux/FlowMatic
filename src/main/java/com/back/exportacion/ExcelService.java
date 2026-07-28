package com.back.exportacion;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelService {

    public void exportarDatos(String nombreHoja, String[] cabeceras, List<Object[]> filas, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(nombreHoja);

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < cabeceras.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cabeceras[i]);
        }

        int rowIdx = 1;
        for (Object[] fila : filas) {
            Row dataRow = sheet.createRow(rowIdx++);
            for (int i = 0; i < fila.length; i++) {
                Cell cell = dataRow.createCell(i);
                if (fila[i] == null) {
                    cell.setCellValue("");
                } else if (fila[i] instanceof Number) {
                    cell.setCellValue(((Number) fila[i]).doubleValue());
                } else if (fila[i] instanceof Boolean) {
                    cell.setCellValue((Boolean) fila[i] ? "Sí" : "No");
                } else {
                    cell.setCellValue(fila[i].toString());
                }
            }
        }

        // Auto-size columns
        for (int i = 0; i < cabeceras.length; i++) {
            sheet.autoSizeColumn(i);
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportarReporte(Map<String, Object> metricas, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Reporte del Sistema");

        var titleRow = sheet.createRow(0);
        var titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Reporte del Sistema \u2014 Flowmatic");

        var dateRow = sheet.createRow(1);
        var dateCell = dateRow.createCell(0);
        dateCell.setCellValue("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm")));

        sheet.createRow(2);

        var headerRow = sheet.createRow(3);
        var h1 = headerRow.createCell(0);
        h1.setCellValue("M\u00e9trica");
        var h2 = headerRow.createCell(1);
        h2.setCellValue("Valor");

        String[][] pares = {
            {"Total usuarios",      String.valueOf(metricas.getOrDefault("totalUsuarios", 0))},
            {"Usuarios RRHH",       String.valueOf(metricas.getOrDefault("totalRRHH", 0))},
            {"Usuarios activos",    String.valueOf(metricas.getOrDefault("totalActivos", 0))},
            {"Pendientes",          String.valueOf(metricas.getOrDefault("totalPendientes", 0))},
            {"Candidatos",          String.valueOf(metricas.getOrDefault("totalCandidatos", 0))},
            {"Administradores",     String.valueOf(metricas.getOrDefault("totalAdmins", 0))},
        };

        int rowIdx = 4;
        for (var par : pares) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(par[0]);
            r.createCell(1).setCellValue(par[1]);
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}