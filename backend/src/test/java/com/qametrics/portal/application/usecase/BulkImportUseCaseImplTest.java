package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.TestExecution;
import com.qametrics.portal.domain.port.outbound.BugRepository;
import com.qametrics.portal.domain.port.outbound.DeliverySlaRepository;
import com.qametrics.portal.domain.port.outbound.TestExecutionRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkImportUseCaseImplTest {

    @Mock
    private TestExecutionRepository executionRepository;

    @Mock
    private BugRepository bugRepository;

    @Mock
    private DeliverySlaRepository deliverySlaRepository;

    @InjectMocks
    private BulkImportUseCaseImpl bulkImportUseCase;

    private byte[] create9ColumnExcelBytes(String dateStr) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Executions");
            Row header = sheet.createRow(0);
            String[] headers = {"ID Jira *", "Línea (FABRICA/MINOR_DEMAND)", "Analista QA *", "Casos Diseñados *", "Casos OK Exitosos", "Casos Fallidos", "Casos Bloqueados", "Sprint/PI", "Fecha de Ejecución (YYYY-MM-DD)"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("JIRA-PAST-01");
            dataRow.createCell(1).setCellValue("FABRICA");
            dataRow.createCell(2).setCellValue("Analista Mayo");
            dataRow.createCell(3).setCellValue(20);
            dataRow.createCell(4).setCellValue(18);
            dataRow.createCell(5).setCellValue(2);
            dataRow.createCell(6).setCellValue(0);
            dataRow.createCell(7).setCellValue("Sprint Mayo");
            dataRow.createCell(8).setCellValue(dateStr);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] create10ColumnExcelBytes(String dateStr) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Executions");
            Row header = sheet.createRow(0);
            String[] headers = {"ID Jira *", "Línea (FABRICA/MINOR_DEMAND)", "Analista QA *", "Casos Diseñados *", "Total Ejecuciones *", "Casos OK Exitosos", "Casos Fallidos", "Casos Bloqueados", "Sprint/PI", "Fecha de Ejecución (YYYY-MM-DD)"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("JIRA-PAST-02");
            dataRow.createCell(1).setCellValue("MINOR_DEMAND");
            dataRow.createCell(2).setCellValue("Analista Junio");
            dataRow.createCell(3).setCellValue(30);
            dataRow.createCell(4).setCellValue(30);
            dataRow.createCell(5).setCellValue(28);
            dataRow.createCell(6).setCellValue(1);
            dataRow.createCell(7).setCellValue(1);
            dataRow.createCell(8).setCellValue("Sprint Junio");
            dataRow.createCell(9).setCellValue(dateStr);

            wb.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createBugsExcelBytes(String dateStr) throws Exception {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Bugs");
            Row header = sheet.createRow(0);
            String[] headers = {"ID Bug Jira *", "Requerimiento (HU) *", "Línea (FABRICA/MINOR_DEMAND)", "Tipo Defecto (FUNCTIONAL/UI_UX)", "Reportado Por", "Desarrollador Asignado", "Sprint/PI", "Fecha de Reporte (YYYY-MM-DD)"};
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);

            Row dataRow = sheet.createRow(1);
            dataRow.createCell(0).setCellValue("BUG-PAST-01");
            dataRow.createCell(1).setCellValue("JIRA-101");
            dataRow.createCell(2).setCellValue("FABRICA");
            dataRow.createCell(3).setCellValue("FUNCTIONAL");
            dataRow.createCell(4).setCellValue("Analista Mayo");
            dataRow.createCell(5).setCellValue("Pedro Dev");
            dataRow.createCell(6).setCellValue("Sprint Mayo");
            dataRow.createCell(7).setCellValue(dateStr);

            wb.write(out);
            return out.toByteArray();
        }
    }

    @Test
    @DisplayName("Debe importar correctamente un Excel de ejecuciones de 9 columnas con fecha de mes pasado (Mayo)")
    void importExecutions_9Columns_PastDate() throws Exception {
        byte[] excelContent = create9ColumnExcelBytes("2026-05-15");
        MockMultipartFile file = new MockMultipartFile("file", "test_may.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelContent);

        Map<String, Object> result = bulkImportUseCase.importExecutions(file);

        assertEquals(1, result.get("importedCount"));
        List<?> errors = (List<?>) result.get("errors");
        assertTrue(errors.isEmpty(), "No deben ocurrir errores al importar 9 columnas");

        ArgumentCaptor<TestExecution> captor = ArgumentCaptor.forClass(TestExecution.class);
        verify(executionRepository, times(1)).save(captor.capture());

        TestExecution saved = captor.getValue();
        assertEquals("JIRA-PAST-01", saved.getJiraId());
        assertEquals(ProjectType.FABRICA, saved.getProjectType());
        assertEquals(LocalDate.of(2026, 5, 15), saved.getAssignmentDate());
        assertEquals(LocalDate.of(2026, 5, 15), saved.getDesignDate());

        assertNotNull(saved.getRuns());
        assertEquals(1, saved.getRuns().size());
        assertEquals(LocalDate.of(2026, 5, 15), saved.getRuns().get(0).getExecutionDate());
    }

    @Test
    @DisplayName("Debe importar correctamente un Excel de ejecuciones de 10 columnas con fecha de mes pasado (Junio)")
    void importExecutions_10Columns_PastDate() throws Exception {
        byte[] excelContent = create10ColumnExcelBytes("2026-06-10");
        MockMultipartFile file = new MockMultipartFile("file", "test_june.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelContent);

        Map<String, Object> result = bulkImportUseCase.importExecutions(file);

        assertEquals(1, result.get("importedCount"));
        List<?> errors = (List<?>) result.get("errors");
        assertTrue(errors.isEmpty(), "No deben ocurrir errores al importar 10 columnas");

        ArgumentCaptor<TestExecution> captor = ArgumentCaptor.forClass(TestExecution.class);
        verify(executionRepository, times(1)).save(captor.capture());

        TestExecution saved = captor.getValue();
        assertEquals("JIRA-PAST-02", saved.getJiraId());
        assertEquals(ProjectType.MINOR_DEMAND, saved.getProjectType());
        assertEquals(LocalDate.of(2026, 6, 10), saved.getAssignmentDate());

        assertEquals(1, saved.getRuns().size());
        assertEquals(LocalDate.of(2026, 6, 10), saved.getRuns().get(0).getExecutionDate());
    }

    @Test
    @DisplayName("Debe importar correctamente un Excel de Bugs conservando la fecha de reporte de meses pasados")
    void importBugs_PastDate() throws Exception {
        byte[] excelContent = createBugsExcelBytes("2026-05-20");
        MockMultipartFile file = new MockMultipartFile("file", "bugs_may.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelContent);

        Map<String, Object> result = bulkImportUseCase.importBugs(file);

        assertEquals(1, result.get("importedCount"));
        List<?> errors = (List<?>) result.get("errors");
        assertTrue(errors.isEmpty(), "No deben ocurrir errores al importar bugs con fecha pasada");

        ArgumentCaptor<Bug> captor = ArgumentCaptor.forClass(Bug.class);
        verify(bugRepository, times(1)).save(captor.capture());

        Bug saved = captor.getValue();
        assertEquals("BUG-PAST-01", saved.getBugJiraId());
        assertEquals("JIRA-101", saved.getRequirementId());
        assertEquals(LocalDate.of(2026, 5, 20), saved.getReportedDate());
    }
}
