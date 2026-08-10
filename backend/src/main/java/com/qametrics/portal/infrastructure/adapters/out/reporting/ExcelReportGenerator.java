package com.qametrics.portal.infrastructure.adapters.out.reporting;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.ClientDeliveryMetric;
import com.qametrics.portal.domain.model.ClientReturn;
import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.TestExecution;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Generador de Libros Excel (.xlsx) usando Apache POI.
 */
@Component
public class ExcelReportGenerator {

    public byte[] generateExcel(List<TestExecution> executions, List<Bug> bugs, List<DeliverySla> deliveries) {
        return generateExcel(executions, bugs, deliveries, List.of(), List.of());
    }

    public byte[] generateExcel(List<TestExecution> executions,
                                List<Bug> bugs,
                                List<DeliverySla> deliveries,
                                List<ClientDeliveryMetric> metrics,
                                List<ClientReturn> returns) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);

            // ── Hoja 1: Resumen Consolidado ──
            Sheet summarySheet = workbook.createSheet("Resumen Consolidado");
            createSummarySheet(summarySheet, workbook, executions, bugs, deliveries, metrics, returns);

            // ── Hoja 2: Ejecuciones ──
            Sheet execSheet = workbook.createSheet("Ejecuciones de Prueba");
            createExecutionsSheet(execSheet, headerStyle, executions);

            // ── Hoja 3: BugTracker ──
            Sheet bugSheet = workbook.createSheet("BugTracker Incidencias");
            createBugsSheet(bugSheet, headerStyle, bugs);

            // ── Hoja 4: Gobierno SLA ──
            Sheet slaSheet = workbook.createSheet("Gobierno SLA Entregas");
            createSlaSheet(slaSheet, headerStyle, deliveries);

            // ── Hoja 5: Seguimiento Cliente & Calidad ──
            Sheet clientSheet = workbook.createSheet("Seguimiento Cliente & Calidad");
            createClientTrackingSheet(clientSheet, headerStyle, metrics, returns);

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando libro Excel: " + e.getMessage(), e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private void createSummarySheet(Sheet sheet, Workbook workbook, List<TestExecution> executions, List<Bug> bugs, List<DeliverySla> deliveries, List<ClientDeliveryMetric> metrics, List<ClientReturn> returns) {
        Row r0 = sheet.createRow(0);
        r0.createCell(0).setCellValue("MÉTRICAS CONSOLIDADAS QA PORTAL");

        long totalOk = executions.stream().mapToLong(TestExecution::getSuccessfulCases).sum();
        long reinjections = bugs.stream().filter(Bug::isReinjectionFlag).count();
        double qualityPct = totalOk > 0 ? (1.0 - ((double) bugs.size() / totalOk)) * 100.0 : 100.0;

        long totalDeliveries = metrics.stream().mapToLong(ClientDeliveryMetric::getTotalDeliveries).sum();
        long qualityReturns = returns.stream().filter(ClientReturn::isCountedInQuality).mapToLong(ClientReturn::getReturnCount).sum();
        String clientQualityStr = totalDeliveries > 0 ? String.format("%.2f%%", (1.0 - ((double) qualityReturns / totalDeliveries)) * 100.0) : "N/A (Sin Entregas)";

        String[][] kpis = {
                {"Total Ejecuciones Realizadas", String.valueOf(executions.size())},
                {"Casos Exitosos (OK)", String.valueOf(totalOk)},
                {"Total Defectos Reportados", String.valueOf(bugs.size())},
                {"Reinyecciones Detectadas (RF03)", String.valueOf(reinjections)},
                {"Porcentaje de Calidad QA (Target 95%)", String.format("%.2f%%", qualityPct)},
                {"Total Hitos de Entrega SLA", String.valueOf(deliveries.size())},
                {"Total Entregas Cliente (Evol+Sop+SC)", String.valueOf(totalDeliveries)},
                {"Devoluciones IBL con Afectación", String.valueOf(qualityReturns)},
                {"Porcentaje Calidad Cliente (Target 95%)", clientQualityStr}
        };

        int rowIdx = 2;
        for (String[] kpi : kpis) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(kpi[0]);
            row.createCell(1).setCellValue(kpi[1]);
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void createExecutionsSheet(Sheet sheet, CellStyle headerStyle, List<TestExecution> executions) {
        String[] headers = {"ID Jira", "Sprint/PI", "Línea", "Analista", "Diseñados", "OK", "Fail", "Block", "Ratio %"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (TestExecution e : executions) {
            double cov = e.getTotalCases() > 0 ? ((double) e.getSuccessfulCases() / e.getTotalCases()) * 100.0 : 0.0;
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(e.getJiraId());
            row.createCell(1).setCellValue(e.getSprintOrPi() != null ? e.getSprintOrPi() : "");
            row.createCell(2).setCellValue(e.getProjectType().name());
            row.createCell(3).setCellValue(e.getDesignerAnalyst());
            row.createCell(4).setCellValue(e.getTotalCases());
            row.createCell(5).setCellValue(e.getSuccessfulCases());
            row.createCell(6).setCellValue(e.getFailedCases());
            row.createCell(7).setCellValue(e.getBlockedCases());
            row.createCell(8).setCellValue(cov);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void createBugsSheet(Sheet sheet, CellStyle headerStyle, List<Bug> bugs) {
        String[] headers = {"Bug ID Jira", "HU Requerimiento", "Línea", "Tipo Defecto", "Reinyección?", "Reportado Por", "Estado"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (Bug b : bugs) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(b.getBugJiraId());
            row.createCell(1).setCellValue(b.getRequirementId());
            row.createCell(2).setCellValue(b.getProjectType().name());
            row.createCell(3).setCellValue(b.getDefectType().name());
            row.createCell(4).setCellValue(b.isReinjectionFlag() ? "SÍ" : "NO");
            row.createCell(5).setCellValue(b.getReportedBy());
            row.createCell(6).setCellValue(b.getStatus().name());
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void createSlaSheet(Sheet sheet, CellStyle headerStyle, List<DeliverySla> deliveries) {
        String[] headers = {"Jira ID", "Línea", "Analista", "Est. Entrega Cliente", "Est. QA", "Real QA", "Real Entrega Cliente", "Estatus SLA", "Días Atraso"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 1;
        for (DeliverySla s : deliveries) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(s.getJiraId());
            row.createCell(1).setCellValue(s.getProjectType().name());
            row.createCell(2).setCellValue(s.getDesignerAnalyst());
            row.createCell(3).setCellValue(s.getEstimatedDeliveryDate() != null ? s.getEstimatedDeliveryDate().toString() : "");
            row.createCell(4).setCellValue(s.getEstimatedQaDate() != null ? s.getEstimatedQaDate().toString() : "");
            row.createCell(5).setCellValue(s.getRealQaDate() != null ? s.getRealQaDate().toString() : "");
            row.createCell(6).setCellValue(s.getRealClientDeliveryDate() != null ? s.getRealClientDeliveryDate().toString() : "");
            row.createCell(7).setCellValue(s.getStatus().name());
            row.createCell(8).setCellValue(s.getDelayDays());
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void createClientTrackingSheet(Sheet sheet, CellStyle headerStyle, List<ClientDeliveryMetric> metrics, List<ClientReturn> returns) {
        // Sección 1: Entregas del Cliente
        Row title1 = sheet.createRow(0);
        title1.createCell(0).setCellValue("1. REGISTROS DE ENTREGAS DEL CLIENTE");

        String[] metricHeaders = {"Periodo / Semana", "Año", "Mes", "Línea Proyecto", "Evolutivos", "Soportes", "Standard Change", "Total Entregas"};
        Row hRow1 = sheet.createRow(1);
        for (int i = 0; i < metricHeaders.length; i++) {
            Cell cell = hRow1.createCell(i);
            cell.setCellValue(metricHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowIdx = 2;
        for (ClientDeliveryMetric m : metrics) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(m.getSprintOrPeriod() != null ? m.getSprintOrPeriod() : "N/A");
            row.createCell(1).setCellValue(m.getYear());
            row.createCell(2).setCellValue(m.getMonth());
            row.createCell(3).setCellValue(m.getProjectType() != null ? m.getProjectType().name() : "");
            row.createCell(4).setCellValue(m.getEvolutivosCount());
            row.createCell(5).setCellValue(m.getSoportesCount());
            row.createCell(6).setCellValue(m.getStandardChangeCount());
            row.createCell(7).setCellValue(m.getTotalDeliveries());
        }

        rowIdx += 2;
        // Sección 2: Devoluciones IBL
        Row title2 = sheet.createRow(rowIdx++);
        title2.createCell(0).setCellValue("2. DEVOLUCIONES DEL CLIENTE (IBL)");

        String[] returnHeaders = {"Código IBL", "Año", "Mes", "Línea Proyecto", "Categoría", "Iteración #", "Afecta Calidad?", "Causa Raíz"};
        Row hRow2 = sheet.createRow(rowIdx++);
        for (int i = 0; i < returnHeaders.length; i++) {
            Cell cell = hRow2.createCell(i);
            cell.setCellValue(returnHeaders[i]);
            cell.setCellStyle(headerStyle);
        }

        for (ClientReturn r : returns) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(r.getIbl());
            row.createCell(1).setCellValue(r.getYear());
            row.createCell(2).setCellValue(r.getMonth());
            row.createCell(3).setCellValue(r.getProjectType() != null ? r.getProjectType().name() : "");
            row.createCell(4).setCellValue(r.getCategory() != null ? r.getCategory().name() : "");
            row.createCell(5).setCellValue(r.getReturnCount());
            row.createCell(6).setCellValue(r.isCountedInQuality() ? "SÍ (2ª+ Vez)" : "NO (1ª Vez)");
            row.createCell(7).setCellValue(r.getRootCause() != null ? r.getRootCause() : "");
        }

        for (int i = 0; i < 8; i++) sheet.autoSizeColumn(i);
    }
}
