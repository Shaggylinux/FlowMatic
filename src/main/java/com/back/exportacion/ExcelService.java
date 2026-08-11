package com.back.exportacion;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelService {

    private static final XSSFColor COLOR_PRIMARY_TEAL = new XSSFColor(new java.awt.Color(13, 148, 136), null);
    private static final XSSFColor COLOR_DARK_TITLE = new XSSFColor(new java.awt.Color(15, 23, 42), null);
    private static final XSSFColor COLOR_MUTED_TEXT = new XSSFColor(new java.awt.Color(100, 116, 139), null);
    private static final XSSFColor COLOR_ZEBRA_ODD = new XSSFColor(new java.awt.Color(248, 250, 252), null);
    private static final XSSFColor COLOR_BORDER = new XSSFColor(new java.awt.Color(203, 213, 225), null);
    private static final XSSFColor COLOR_SUMMARY_BG = new XSSFColor(new java.awt.Color(240, 253, 250), null);

    // Status colors
    private static final XSSFColor COLOR_GREEN_BG = new XSSFColor(new java.awt.Color(220, 252, 231), null);
    private static final XSSFColor COLOR_GREEN_TEXT = new XSSFColor(new java.awt.Color(22, 101, 52), null);

    private static final XSSFColor COLOR_AMBER_BG = new XSSFColor(new java.awt.Color(254, 243, 199), null);
    private static final XSSFColor COLOR_AMBER_TEXT = new XSSFColor(new java.awt.Color(146, 64, 14), null);

    private static final XSSFColor COLOR_RED_BG = new XSSFColor(new java.awt.Color(254, 226, 226), null);
    private static final XSSFColor COLOR_RED_TEXT = new XSSFColor(new java.awt.Color(153, 27, 27), null);

    public void exportarDatos(String nombreHoja, String[] cabeceras, List<Object[]> filas, HttpServletResponse response) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(nombreHoja);
        sheet.setDisplayGridlines(true);

        byte[] logoBytes = cargarLogoBytes();
        int titleStartCol = 0;

        if (logoBytes != null && logoBytes.length > 0) {
            try {
                int pictureIdx = workbook.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
                CreationHelper helper = workbook.getCreationHelper();
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(0);
                anchor.setRow1(0);
                anchor.setCol2(2);
                anchor.setRow2(3);
                drawing.createPicture(anchor, pictureIdx);
                titleStartCol = 2;
            } catch (Exception ignored) {
            }
        }

        // Title and Metadata
        Row row0 = sheet.createRow(0);
        row0.setHeightInPoints(22);

        Row row1 = sheet.createRow(1);
        row1.setHeightInPoints(24);
        Cell titleCell = row1.createCell(titleStartCol);
        titleCell.setCellValue("FLOWMATIC — REPORTE DE " + nombreHoja.toUpperCase());
        titleCell.setCellStyle(crearEstiloTitulo(workbook));

        Row row2 = sheet.createRow(2);
        row2.setHeightInPoints(18);
        Cell subCell = row2.createCell(titleStartCol);
        subCell.setCellValue("Generado el: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm")) + " | Sistema FlowMatic");
        subCell.setCellStyle(crearEstiloSubtitulo(workbook));

        Row row3 = sheet.createRow(3);
        row3.setHeightInPoints(12);

        // Header Row
        Row headerRow = sheet.createRow(4);
        headerRow.setHeightInPoints(26);
        XSSFCellStyle headerStyle = crearEstiloCabecera(workbook);

        for (int i = 0; i < cabeceras.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(cabeceras[i]);
            cell.setCellStyle(headerStyle);
        }

        // Styles for data rows
        XSSFCellStyle dataStyleEven = crearEstiloDato(workbook, false);
        XSSFCellStyle dataStyleOdd = crearEstiloDato(workbook, true);
        XSSFCellStyle dataStyleEvenCenter = crearEstiloDatoCentrado(workbook, false);
        XSSFCellStyle dataStyleOddCenter = crearEstiloDatoCentrado(workbook, true);

        XSSFCellStyle styleGreen = crearEstiloEstado(workbook, COLOR_GREEN_BG, COLOR_GREEN_TEXT);
        XSSFCellStyle styleAmber = crearEstiloEstado(workbook, COLOR_AMBER_BG, COLOR_AMBER_TEXT);
        XSSFCellStyle styleRed = crearEstiloEstado(workbook, COLOR_RED_BG, COLOR_RED_TEXT);

        int rowIdx = 5;
        if (filas != null) {
            for (Object[] fila : filas) {
                Row dataRow = sheet.createRow(rowIdx++);
                dataRow.setHeightInPoints(22);
                boolean isOdd = ((rowIdx - 5) % 2 != 0);

                for (int i = 0; i < fila.length; i++) {
                    Cell cell = dataRow.createCell(i);
                    Object val = fila[i];
                    String strVal = (val != null) ? val.toString().trim() : "";

                    XSSFCellStyle baseStyle = isOdd ? dataStyleOdd : dataStyleEven;

                    if (val == null) {
                        cell.setCellValue("");
                        cell.setCellStyle(baseStyle);
                    } else if (esEstadoVerde(strVal)) {
                        cell.setCellValue(strVal);
                        cell.setCellStyle(styleGreen);
                    } else if (esEstadoAmarillo(strVal)) {
                        cell.setCellValue(strVal);
                        cell.setCellStyle(styleAmber);
                    } else if (esEstadoRojo(strVal)) {
                        cell.setCellValue(strVal);
                        cell.setCellStyle(styleRed);
                    } else if (val instanceof Number) {
                        cell.setCellValue(((Number) val).doubleValue());
                        XSSFCellStyle numStyle = workbook.createCellStyle();
                        numStyle.cloneStyleFrom(baseStyle);
                        numStyle.setAlignment(HorizontalAlignment.RIGHT);
                        cell.setCellStyle(numStyle);
                    } else if (val instanceof Boolean) {
                        cell.setCellValue((Boolean) val ? "Sí" : "No");
                        cell.setCellStyle(isOdd ? dataStyleOddCenter : dataStyleEvenCenter);
                    } else {
                        cell.setCellValue(strVal);
                        cell.setCellStyle(baseStyle);
                    }
                }
            }
        }

        // Summary row
        Row summaryRow = sheet.createRow(rowIdx++);
        summaryRow.setHeightInPoints(24);
        XSSFCellStyle summaryStyle = crearEstiloResumen(workbook);

        Cell sumCellLbl = summaryRow.createCell(0);
        sumCellLbl.setCellValue("TOTAL DE REGISTROS");
        sumCellLbl.setCellStyle(summaryStyle);

        Cell sumCellVal = summaryRow.createCell(1);
        sumCellVal.setCellValue(filas != null ? filas.size() : 0);
        sumCellVal.setCellStyle(summaryStyle);

        for (int i = 2; i < cabeceras.length; i++) {
            Cell emptySum = summaryRow.createCell(i);
            emptySum.setCellValue("");
            emptySum.setCellStyle(summaryStyle);
        }

        // Auto-fit columns with extra padding
        for (int i = 0; i < cabeceras.length; i++) {
            sheet.autoSizeColumn(i);
            int width = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, Math.max(width + 1600, 3800));
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }

    public void exportarReporte(Map<String, Object> metricas, HttpServletResponse response) throws IOException {
        String[] cabeceras = {"Métrica", "Valor"};
        String[][] pares = {
            {"Total usuarios",      String.valueOf(metricas.getOrDefault("totalUsuarios", 0))},
            {"Usuarios RRHH",       String.valueOf(metricas.getOrDefault("totalRRHH", 0))},
            {"Usuarios activos",    String.valueOf(metricas.getOrDefault("totalActivos", 0))},
            {"Pendientes de activación", String.valueOf(metricas.getOrDefault("totalPendientes", 0))},
            {"Usuarios bloqueados", String.valueOf(metricas.getOrDefault("totalBloqueados", 0))},
            {"Candidatos registrados", String.valueOf(metricas.getOrDefault("totalCandidatos", 0))},
            {"Administradores",     String.valueOf(metricas.getOrDefault("totalAdmins", 0))},
        };

        List<Object[]> filas = java.util.Arrays.stream(pares)
            .map(p -> new Object[]{p[0], Integer.parseInt(p[1])})
            .toList();

        exportarDatos("Reporte General", cabeceras, filas, response);
    }

    private byte[] cargarLogoBytes() {
        try (InputStream is = getClass().getResourceAsStream("/img/Flowmatic-excel.png")) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (Exception ignored) {
        }
        try {
            Path path = Paths.get("src/main/resources/img/Flowmatic-excel.png");
            if (Files.exists(path)) {
                return Files.readAllBytes(path);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private XSSFCellStyle crearEstiloTitulo(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        font.setColor(COLOR_PRIMARY_TEAL);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle crearEstiloSubtitulo(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        font.setColor(COLOR_MUTED_TEXT);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle crearEstiloCabecera(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(COLOR_PRIMARY_TEAL);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        font.setColor(new XSSFColor(new java.awt.Color(255, 255, 255), null));
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(style, COLOR_PRIMARY_TEAL);
        return style;
    }

    private XSSFCellStyle crearEstiloDato(XSSFWorkbook workbook, boolean isOdd) {
        XSSFCellStyle style = workbook.createCellStyle();
        if (isOdd) {
            style.setFillForegroundColor(COLOR_ZEBRA_ODD);
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        XSSFFont font = workbook.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setColor(COLOR_DARK_TITLE);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(style, COLOR_BORDER);
        return style;
    }

    private XSSFCellStyle crearEstiloDatoCentrado(XSSFWorkbook workbook, boolean isOdd) {
        XSSFCellStyle style = crearEstiloDato(workbook, isOdd);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private XSSFCellStyle crearEstiloEstado(XSSFWorkbook workbook, XSSFColor bg, XSSFColor text) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(bg);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(text);
        style.setFont(font);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        aplicarBordes(style, COLOR_BORDER);
        return style;
    }

    private XSSFCellStyle crearEstiloResumen(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(COLOR_SUMMARY_BG);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont font = workbook.createFont();
        font.setFontName("Segoe UI");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        font.setColor(COLOR_PRIMARY_TEAL);
        style.setFont(font);

        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.DOUBLE);
        style.setTopBorderColor(COLOR_PRIMARY_TEAL);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(COLOR_BORDER);
        return style;
    }

    private void aplicarBordes(XSSFCellStyle style, XSSFColor borderColor) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(borderColor);
        style.setBottomBorderColor(borderColor);
        style.setLeftBorderColor(borderColor);
        style.setRightBorderColor(borderColor);
    }

    private boolean esEstadoVerde(String v) {
        if (v == null) return false;
        String s = v.trim().toLowerCase();
        return s.equals("activo") || s.equals("aprobado") || s.equals("contratado") || s.equals("sí") || s.equals("si") || s.equals("confirmado");
    }

    private boolean esEstadoAmarillo(String v) {
        if (v == null) return false;
        String s = v.trim().toLowerCase();
        return s.equals("pendiente") || s.equals("en pruebas") || s.equals("reprogramado") || s.equals("opcional");
    }

    private boolean esEstadoRojo(String v) {
        if (v == null) return false;
        String s = v.trim().toLowerCase();
        return s.equals("bloqueado") || s.equals("rechazado") || s.equals("cancelado") || s.equals("no aceptado") || s.equals("no") || s.equals("descartado");
    }
}