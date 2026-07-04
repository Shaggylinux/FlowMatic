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
import jakarta.servlet.http.HttpServletResponse;

@Service
public class ExcelService {
    public void exportarUsuarios(List<Usuario> usuarios, HttpServletResponse response) throws IOException {
        Workbook workbook = new XSSFWorkbook();

        Sheet sheet = workbook.createSheet("Usuarios");

        Row row = sheet.createRow(0);
        String[] columnas = {"ID", "Usuario", "Apellido", "Email", "Rol", "Estado"};
        
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
            dataRow.createCell(5).setCellValue(u.isActivo() ? "Activo" : "Pendiente");
        }

        workbook.write(response.getOutputStream());
        workbook.close();
    }
}