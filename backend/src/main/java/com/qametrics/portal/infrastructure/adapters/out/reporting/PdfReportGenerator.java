package com.qametrics.portal.infrastructure.adapters.out.reporting;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.ClientDeliveryMetric;
import com.qametrics.portal.domain.model.ClientReturn;
import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.TestExecution;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generador de Reportes PDF usando OpenPDF.
 */
@Component
public class PdfReportGenerator {

    public byte[] generatePdf(List<TestExecution> executions, List<Bug> bugs, List<DeliverySla> deliveries, String projectType) {
        return generatePdf(executions, bugs, deliveries, List.of(), List.of(), projectType);
    }

    public byte[] generatePdf(List<TestExecution> executions,
                              List<Bug> bugs,
                              List<DeliverySla> deliveries,
                              List<ClientDeliveryMetric> metrics,
                              List<ClientReturn> returns,
                              String projectType) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // ── Título y Header ──
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(99, 102, 241));
            Paragraph title = new Paragraph("QA METRICS PORTAL — INFORME EJECUTIVO DE CALIDAD & SLA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            Paragraph sub = new Paragraph("Generado el: " + dateStr + " | Línea: " + (projectType == null || projectType.isEmpty() ? "CONSOLIDADO GENERAL" : projectType), subFont);
            sub.setAlignment(Element.ALIGN_CENTER);
            sub.setSpacingAfter(15);
            document.add(sub);

            // ── Resumen KPI ──
            int totalExecutions = executions.size();
            long totalOk = executions.stream().mapToLong(TestExecution::getSuccessfulCases).sum();
            long reinjections = bugs.stream().filter(Bug::isReinjectionFlag).count();

            double ratio = totalExecutions > 0 ? ((double) totalOk / totalExecutions) * 100.0 : 0.0;
            double qualityPercentage = totalOk > 0 ? (1.0 - ((double) bugs.size() / totalOk)) * 100.0 : 100.0;

            long totalClientDeliveries = metrics.stream().mapToLong(ClientDeliveryMetric::getTotalDeliveries).sum();
            long qualityReturns = returns.stream().filter(ClientReturn::isCountedInQuality).mapToLong(ClientReturn::getReturnCount).sum();
            double clientQuality = totalClientDeliveries > 0 ? (1.0 - ((double) qualityReturns / totalClientDeliveries)) * 100.0 : -1.0;

            PdfPTable kpiTable = new PdfPTable(4);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(20);

            addKpiCell(kpiTable, "Total Ejecuciones", String.valueOf(totalExecutions), new Color(238, 242, 255));
            addKpiCell(kpiTable, "Casos OK Exitosos", String.valueOf(totalOk), new Color(236, 253, 245));
            addKpiCell(kpiTable, "% Ratio Ejecución", String.format("%.1f%%", ratio), new Color(245, 243, 255));
            addKpiCell(kpiTable, "% Calidad QA (95%)", String.format("%.1f%%", qualityPercentage), qualityPercentage >= 95 ? new Color(236, 253, 245) : new Color(254, 242, 242));

            document.add(kpiTable);

            // ── Sección 1: Ejecuciones de Prueba ──
            Font secFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(30, 41, 59));
            Paragraph p1 = new Paragraph("1. Ejecuciones de Prueba y Ratio de Cobertura", secFont);
            p1.setSpacingAfter(8);
            document.add(p1);

            PdfPTable execTable = new PdfPTable(5);
            execTable.setWidthPercentage(100);
            execTable.setSpacingAfter(20);
            addTableHeader(execTable, new String[]{"ID Jira", "Línea", "Analista", "Casos OK / Totales", "Ratio %"});

            for (TestExecution e : executions) {
                double cov = e.getTotalCases() > 0 ? ((double) e.getSuccessfulCases() / e.getTotalCases()) * 100.0 : 0.0;
                addTableCell(execTable, e.getJiraId());
                addTableCell(execTable, e.getProjectType().name());
                addTableCell(execTable, e.getDesignerAnalyst());
                addTableCell(execTable, e.getSuccessfulCases() + " / " + e.getTotalCases());
                addTableCell(execTable, String.format("%.1f%%", cov));
            }
            document.add(execTable);

            // ── Sección 2: BugTracker y Reinyecciones (RF03) ──
            Paragraph p2 = new Paragraph("2. BugTracker & Detección de Reinyecciones (RF03)", secFont);
            p2.setSpacingAfter(8);
            document.add(p2);

            PdfPTable bugTable = new PdfPTable(5);
            bugTable.setWidthPercentage(100);
            bugTable.setSpacingAfter(20);
            addTableHeader(bugTable, new String[]{"Bug ID", "Requerimiento", "Tipo Defecto", "Reinyección?", "Estado"});

            for (Bug b : bugs) {
                addTableCell(bugTable, b.getBugJiraId());
                addTableCell(bugTable, b.getRequirementId());
                addTableCell(bugTable, b.getDefectType().name());
                addTableCell(bugTable, b.isReinjectionFlag() ? "🚨 SÍ (REINYECCIÓN)" : "NO");
                addTableCell(bugTable, b.getStatus().name());
            }
            document.add(bugTable);

            // ── Sección 3: Gobierno SLA (RF04) ──
            Paragraph p3 = new Paragraph("3. Seguimiento de Entregas & Gobierno SLA (RF04)", secFont);
            p3.setSpacingAfter(8);
            document.add(p3);

            PdfPTable slaTable = new PdfPTable(5);
            slaTable.setWidthPercentage(100);
            slaTable.setSpacingAfter(20);
            addTableHeader(slaTable, new String[]{"Jira ID", "Est. Cliente", "Real Cliente", "Estatus SLA", "Desviación"});

            for (DeliverySla s : deliveries) {
                addTableCell(slaTable, s.getJiraId());
                addTableCell(slaTable, s.getEstimatedDeliveryDate() != null ? s.getEstimatedDeliveryDate().toString() : "N/A");
                addTableCell(slaTable, s.getRealClientDeliveryDate() != null ? s.getRealClientDeliveryDate().toString() : "Pendiente");
                addTableCell(slaTable, s.getStatus().name());
                addTableCell(slaTable, s.getDelayDays() > 0 ? "+" + s.getDelayDays() + " Días" : "0 Días");
            }
            document.add(slaTable);

            // ── Sección 4: Seguimiento de Entregas del Cliente & Calidad (RF05) ──
            Paragraph p4 = new Paragraph("4. Seguimiento de Entregas del Cliente & Devoluciones IBL (Target 95%)", secFont);
            p4.setSpacingAfter(8);
            document.add(p4);

            PdfPTable clientKpiTable = new PdfPTable(3);
            clientKpiTable.setWidthPercentage(100);
            clientKpiTable.setSpacingAfter(12);

            addKpiCell(clientKpiTable, "Total Entregas Cliente", String.valueOf(totalClientDeliveries), new Color(241, 245, 249));
            addKpiCell(clientKpiTable, "Devoluciones Afectan Calidad", String.valueOf(qualityReturns), new Color(254, 242, 242));
            addKpiCell(clientKpiTable, "% Calidad Cliente (95%)", clientQuality >= 0 ? String.format("%.1f%%", clientQuality) : "N/A (Sin Entregas)", (clientQuality >= 95 || clientQuality < 0) ? new Color(236, 253, 245) : new Color(254, 242, 242));
            document.add(clientKpiTable);

            if (!returns.isEmpty()) {
                PdfPTable returnTable = new PdfPTable(6);
                returnTable.setWidthPercentage(100);
                returnTable.setSpacingAfter(15);
                addTableHeader(returnTable, new String[]{"Código IBL", "Mes/Año", "Categoría", "Iteración #", "Afecta Calidad?", "Causa Raíz"});

                for (ClientReturn r : returns) {
                    addTableCell(returnTable, r.getIbl());
                    addTableCell(returnTable, "Mes " + r.getMonth() + "/" + r.getYear());
                    addTableCell(returnTable, r.getCategory() != null ? r.getCategory().name() : "");
                    addTableCell(returnTable, "#" + r.getReturnCount());
                    addTableCell(returnTable, r.isCountedInQuality() ? "⚠️ SÍ (2ª+ vez)" : "ℹ️ NO (1ª vez)");
                    addTableCell(returnTable, r.getRootCause() != null ? r.getRootCause() : "N/A");
                }
                document.add(returnTable);
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private void addKpiCell(PdfPTable table, String label, String value, Color bgColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.DARK_GRAY);
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(99, 102, 241));

        Paragraph p = new Paragraph(label + "\n", labelFont);
        p.add(new Chunk(value, valFont));
        cell.addElement(p);
        table.addCell(cell);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setBackgroundColor(new Color(99, 102, 241));
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private void addTableCell(PdfPTable table, String text) {
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", cellFont));
        cell.setPadding(4);
        table.addCell(cell);
    }
}
