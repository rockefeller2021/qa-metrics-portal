// Create test excel file with June dates
import java.io.*;
import java.time.LocalDate;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class MakeJuneExcel {
    public static void main(String[] args) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Executions");
        Row header = sheet.createRow(0);
        String[] headers = {"ID Jira *", "Línea (FABRICA/MINOR_DEMAND)", "Analista QA *", "Casos Diseñados *", "Casos OK Exitosos", "Casos Fallidos", "Casos Bloqueados", "Sprint/PI", "Fecha de Ejecución (YYYY-MM-DD)"};
        for (int i=0; i<headers.length; i++) header.createCell(i).setCellValue(headers[i]);

        // June 15 execution
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("JIRA-JUNE-01");
        row1.createCell(1).setCellValue("FABRICA");
        row1.createCell(2).setCellValue("Analista Junio");
        row1.createCell(3).setCellValue(25);
        row1.createCell(4).setCellValue(23);
        row1.createCell(5).setCellValue(2);
        row1.createCell(6).setCellValue(0);
        row1.createCell(7).setCellValue("Sprint Junio");
        row1.createCell(8).setCellValue("2026-06-15");

        // July 10 execution
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("JIRA-JULY-01");
        row2.createCell(1).setCellValue("MINOR_DEMAND");
        row2.createCell(2).setCellValue("Analista Julio");
        row2.createCell(3).setCellValue(30);
        row2.createCell(4).setCellValue(28);
        row2.createCell(5).setCellValue(1);
        row2.createCell(6).setCellValue(1);
        row2.createCell(7).setCellValue("Sprint Julio");
        row2.createCell(8).setCellValue("2026-07-10");

        try (FileOutputStream out = new FileOutputStream("test_june_july.xlsx")) {
            wb.write(out);
        }
        System.out.println("Generated test_june_july.xlsx successfully!");
    }
}
