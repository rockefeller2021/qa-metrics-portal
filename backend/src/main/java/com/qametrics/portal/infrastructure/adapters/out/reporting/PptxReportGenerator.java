package com.qametrics.portal.infrastructure.adapters.out.reporting;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.ClientDeliveryMetric;
import com.qametrics.portal.domain.model.ClientReturn;
import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.RunStatus;
import com.qametrics.portal.domain.model.TestExecution;

import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.xslf.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generador de Presentaciones PowerPoint (.pptx) con Gráficos Modernos de Alta Defición (HD Flat Aesthetics).
 */
@Component
public class PptxReportGenerator {

    // Paleta de Colores Ultra-Moderna (Tailwind HSL / Indigo-Purple / Emerald / Rose)
    private static final Color COLOR_PRIMARY = new Color(99, 102, 241);    // Indigo #6366f1
    private static final Color COLOR_SUCCESS = new Color(16, 185, 129);   // Emerald #10b981
    private static final Color COLOR_DANGER  = new Color(244, 63, 94);    // Rose #f43f5e
    private static final Color COLOR_WARNING = new Color(245, 158, 11);   // Amber #f59e0b
    private static final Color COLOR_PURPLE  = new Color(139, 92, 246);   // Purple #8b5cf6
    private static final Color COLOR_TEXT_DARK = new Color(30, 41, 59);   // Slate-800
    private static final Color COLOR_BG_LIGHT  = new Color(248, 250, 252); // Slate-50

    public byte[] generatePptx(List<TestExecution> executions, List<Bug> bugs, List<DeliverySla> deliveries, String projectType) {
        return generatePptx(executions, bugs, deliveries, List.of(), List.of(), projectType);
    }

    public byte[] generatePptx(List<TestExecution> executions,
                               List<Bug> bugs,
                               List<DeliverySla> deliveries,
                               List<ClientDeliveryMetric> metrics,
                               List<ClientReturn> returns,
                               String projectType) {
        try (XMLSlideShow ppt = new XMLSlideShow(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Slide 1: Portada ──
            XSLFSlide slide1 = ppt.createSlide();
            createTitleSlide(slide1, projectType);

            // ── Slide 2: Calidad & Casos de Prueba (Gráfico de Barras Moderno) ──
            XSLFSlide slide2 = ppt.createSlide();
            createQualityAndEffortSlide(ppt, slide2, executions, bugs);

            // ── Slide 3: BugTracker & Reinyecciones (Pie Chart Moderno Donut Flat) ──
            XSLFSlide slide3 = ppt.createSlide();
            createBugTrackerSlide(ppt, slide3, bugs);

            // ── Slide 4: Gobierno SLA (Bar Chart Flat Moderno) ──
            XSLFSlide slide4 = ppt.createSlide();
            createSlaSlide(ppt, slide4, deliveries);

            // ── Slide 5: Seguimiento Cliente & Calidad (Entregas vs Devoluciones) ──
            XSLFSlide slide5 = ppt.createSlide();
            createClientTrackingSlide(ppt, slide5, metrics, returns);

            ppt.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando presentación PowerPoint: " + e.getMessage(), e);
        }
    }

    private void createTitleSlide(XSLFSlide slide, String projectType) {
        XSLFTextBox titleBox = slide.createTextBox();
        titleBox.setAnchor(new Rectangle(50, 140, 620, 120));

        XSLFTextParagraph p1 = titleBox.addNewTextParagraph();
        XSLFTextRun r1 = p1.addNewTextRun();
        r1.setText("QA METRICS PORTAL");
        r1.setFontSize(36.0);
        r1.setBold(true);
        r1.setFontColor(COLOR_PRIMARY);

        XSLFTextParagraph p2 = titleBox.addNewTextParagraph();
        XSLFTextRun r2 = p2.addNewTextRun();
        r2.setText("Informe Ejecutivo de Gobierno QA & SLA");
        r2.setFontSize(22.0);
        r2.setFontColor(COLOR_TEXT_DARK);

        XSLFTextBox dateBox = slide.createTextBox();
        dateBox.setAnchor(new Rectangle(50, 380, 620, 50));
        XSLFTextParagraph p3 = dateBox.addNewTextParagraph();
        XSLFTextRun r3 = p3.addNewTextRun();
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        r3.setText("Fecha de Generación: " + dateStr + " | Línea: " + (projectType == null || projectType.isEmpty() ? "CONSOLIDADO GENERAL" : projectType));
        r3.setFontSize(14.0);
        r3.setFontColor(Color.GRAY);
    }

    private void createQualityAndEffortSlide(XMLSlideShow ppt, XSLFSlide slide, List<TestExecution> executions, List<Bug> bugs) {
        addSlideHeader(slide, "1. Métricas de Calidad (% Target 95%) & Cobertura de Casos");

        long totalOk = executions.stream()
                .mapToLong(e -> {
                    if (e.getSuccessfulCases() > 0) return e.getSuccessfulCases();
                    if (e.getRuns() != null && !e.getRuns().isEmpty()) {
                        return e.getRuns().stream().filter(r -> RunStatus.SUCCESSFUL.equals(r.getStatus())).count();
                    }
                    return 0;
                })
                .sum();

        long totalFail = executions.stream().mapToLong(TestExecution::getFailedCases).sum();
        long totalBlock = executions.stream().mapToLong(TestExecution::getBlockedCases).sum();

        long totalExecutions = executions.stream()
                .mapToLong(e -> {
                    if (e.getRuns() != null && !e.getRuns().isEmpty()) {
                        long runSum = e.getRuns().stream().mapToLong(r -> r.getCasesExecuted() > 0 ? r.getCasesExecuted() : 1).sum();
                        return Math.max(runSum, Math.max(e.getTotalCases(), e.getRuns().size()));
                    }
                    return Math.max(e.getTotalCases(), 1);
                })
                .sum();

        double qualityPct = totalOk > 0 ? (1.0 - ((double) bugs.size() / totalOk)) * 100.0 : 100.0;
        if (qualityPct < 0) qualityPct = 0;

        double effort = totalOk > 0 ? (double) totalExecutions / totalOk : 0.0;

        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle(40, 100, 310, 300));

        addBulletPoint(box, "• % Calidad Actual: ", String.format("%.2f%%", qualityPct) + " (Target 95%)");
        addBulletPoint(box, "• Esfuerzo de Ejecución: ", String.format("%.2f", effort) + " exec/OK");
        addBulletPoint(box, "• Casos OK Exitosos: ", String.valueOf(totalOk) + " casos");
        addBulletPoint(box, "• Casos Fallidos / Bloqueados: ", (totalFail + totalBlock) + " casos");

        // Gráfico de Barras Flat HD
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(totalOk, "Resultados", "Casos OK Exitosos");
            dataset.addValue(totalFail, "Resultados", "Casos Fallidos");
            dataset.addValue(totalBlock, "Resultados", "Casos Bloqueados");

            JFreeChart chart = ChartFactory.createBarChart("Resultado de Ejecución de Casos", "", "Cantidad", dataset);
            styleBarChart(chart, new Color[]{COLOR_PRIMARY, COLOR_DANGER, COLOR_WARNING});

            byte[] chartPng = renderHdChart(chart, 750, 500);
            XSLFPictureData picData = ppt.addPicture(chartPng, PictureData.PictureType.PNG);
            XSLFPictureShape picture = slide.createPicture(picData);
            picture.setAnchor(new Rectangle(350, 95, 340, 255));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createBugTrackerSlide(XMLSlideShow ppt, XSLFSlide slide, List<Bug> bugs) {
        addSlideHeader(slide, "2. BugTracker & Trazabilidad de Reinyecciones (RF03)");

        long reinjections = bugs.stream().filter(Bug::isReinjectionFlag).count();
        long newBugs = bugs.size() - reinjections;
        long openCount = bugs.stream().filter(b -> "OPEN".equalsIgnoreCase(b.getStatus().name()) || "IN_PROGRESS".equalsIgnoreCase(b.getStatus().name())).count();
        long resolvedCount = bugs.size() - openCount;

        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle(40, 100, 310, 300));

        addBulletPoint(box, "• Total Incidencias: ", String.valueOf(bugs.size()) + " bugs");
        addBulletPoint(box, "• Reinyecciones (RF03): ", String.valueOf(reinjections) + " 🚨");
        addBulletPoint(box, "• Bugs Nuevos: ", String.valueOf(newBugs));
        addBulletPoint(box, "• Resueltos / Cerrados: ", String.valueOf(resolvedCount));

        // Gráfico Circular Moderno (Flat Pie Chart)
        try {
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            dataset.setValue("Bugs Nuevos (OK)", newBugs > 0 ? newBugs : 1);
            dataset.setValue("Reinyecciones 🚨", reinjections > 0 ? reinjections : 0);

            JFreeChart chart = ChartFactory.createPieChart("Clasificación de Incidencias (RF03)", dataset, true, true, false);
            stylePieChart(chart);

            byte[] chartPng = renderHdChart(chart, 750, 500);
            XSLFPictureData picData = ppt.addPicture(chartPng, PictureData.PictureType.PNG);
            XSLFPictureShape picture = slide.createPicture(picData);
            picture.setAnchor(new Rectangle(350, 95, 340, 255));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSlaSlide(XMLSlideShow ppt, XSLFSlide slide, List<DeliverySla> deliveries) {
        addSlideHeader(slide, "3. Seguimiento de Entregas & Gobierno SLA (RF04)");

        long onTime = deliveries.stream().filter(d -> "ON_TIME".equalsIgnoreCase(d.getStatus().name())).count();
        long delayed = deliveries.stream().filter(d -> "DELAYED".equalsIgnoreCase(d.getStatus().name())).count();
        long pending = deliveries.stream().filter(d -> "PENDING".equalsIgnoreCase(d.getStatus().name())).count();
        double compliance = deliveries.size() > 0 ? ((double) onTime / deliveries.size()) * 100.0 : 100.0;

        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle(40, 100, 310, 300));

        addBulletPoint(box, "• Cumplimiento SLA: ", String.format("%.1f%%", compliance) + " a tiempo");
        addBulletPoint(box, "• A Tiempo (SLA OK): ", String.valueOf(onTime));
        addBulletPoint(box, "• Retrasados (Fuera SLA): ", String.valueOf(delayed) + " ⚠️");
        addBulletPoint(box, "• Pendientes de Entrega: ", String.valueOf(pending));

        // Gráfico de Barras Moderno Estatus SLA
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(onTime, "SLA", "A Tiempo (OK)");
            dataset.addValue(delayed, "SLA", "Fuera de SLA ⚠️");
            dataset.addValue(pending, "SLA", "Pendientes");

            JFreeChart chart = ChartFactory.createBarChart("Estatus SLA de Entregas", "", "Requerimientos", dataset);
            styleBarChart(chart, new Color[]{COLOR_SUCCESS, COLOR_DANGER, COLOR_PRIMARY});

            byte[] chartPng = renderHdChart(chart, 750, 500);
            XSLFPictureData picData = ppt.addPicture(chartPng, PictureData.PictureType.PNG);
            XSLFPictureShape picture = slide.createPicture(picData);
            picture.setAnchor(new Rectangle(350, 95, 340, 255));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createClientTrackingSlide(XMLSlideShow ppt, XSLFSlide slide, List<ClientDeliveryMetric> metrics, List<ClientReturn> returns) {
        addSlideHeader(slide, "4. Seguimiento de Entregas del Cliente & Devoluciones (RF05)");

        long totalEvolutivos = metrics.stream().mapToLong(ClientDeliveryMetric::getEvolutivosCount).sum();
        long totalSoportes = metrics.stream().mapToLong(ClientDeliveryMetric::getSoportesCount).sum();
        long totalStandardChange = metrics.stream().mapToLong(ClientDeliveryMetric::getStandardChangeCount).sum();
        long totalDeliveries = totalEvolutivos + totalSoportes + totalStandardChange;

        long qualityReturns = returns.stream().filter(ClientReturn::isCountedInQuality).mapToLong(ClientReturn::getReturnCount).sum();
        double clientQuality = totalDeliveries > 0 ? (1.0 - ((double) qualityReturns / totalDeliveries)) * 100.0 : -1.0;

        XSLFTextBox box = slide.createTextBox();
        box.setAnchor(new Rectangle(40, 100, 310, 300));

        addBulletPoint(box, "• Total Entregas Cliente: ", String.valueOf(totalDeliveries) + " entregas");
        addBulletPoint(box, "  - Evolutivos: ", String.valueOf(totalEvolutivos));
        addBulletPoint(box, "  - Soportes: ", String.valueOf(totalSoportes));
        addBulletPoint(box, "  - Standard Change: ", String.valueOf(totalStandardChange));
        addBulletPoint(box, "• Devoluciones (Afectan): ", String.valueOf(qualityReturns) + " IBLs");
        addBulletPoint(box, "• % Calidad Cliente: ", clientQuality >= 0 ? String.format("%.2f%%", clientQuality) + " (Target 95%)" : "N/A (Sin Entregas)");

        // Gráfico de Barras Flat HD
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(totalEvolutivos, "Entregas", "Evolutivos");
            dataset.addValue(totalSoportes, "Entregas", "Soportes");
            dataset.addValue(totalStandardChange, "Entregas", "Standard Change");
            dataset.addValue(qualityReturns, "Entregas", "Devoluciones IBL");

            JFreeChart chart = ChartFactory.createBarChart("Entregas vs Devoluciones IBL", "", "Cantidad", dataset);
            styleBarChart(chart, new Color[]{COLOR_PRIMARY, COLOR_SUCCESS, COLOR_PURPLE, COLOR_DANGER});

            byte[] chartPng = renderHdChart(chart, 750, 500);
            XSLFPictureData picData = ppt.addPicture(chartPng, PictureData.PictureType.PNG);
            XSLFPictureShape picture = slide.createPicture(picData);
            picture.setAnchor(new Rectangle(350, 95, 340, 255));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Estilizador Flat Moderno de JFreeChart ──────────────────────────────
    private void styleBarChart(JFreeChart chart, Color[] colors) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setPaint(COLOR_TEXT_DARK);
        }

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("SansSerif", Font.PLAIN, 12));
            legend.setBorder(0, 0, 0, 0);
            legend.setBackgroundPaint(Color.WHITE);
        }

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(COLOR_BG_LIGHT);
        plot.setOutlineVisible(false);
        plot.setRangeGridlinePaint(new Color(226, 232, 240));

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setTickLabelFont(new Font("SansSerif", Font.BOLD, 12));
        domainAxis.setTickLabelPaint(COLOR_TEXT_DARK);

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 11));
        rangeAxis.setTickLabelPaint(Color.GRAY);
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter()); // Quitar gradientes 3D obsoletos
        renderer.setDrawBarOutline(false);
        renderer.setItemMargin(0.15);

        for (int i = 0; i < colors.length; i++) {
            renderer.setSeriesPaint(i, colors[i]);
        }
    }

    private void stylePieChart(JFreeChart chart) {
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setPaint(COLOR_TEXT_DARK);
        }

        LegendTitle legend = chart.getLegend();
        if (legend != null) {
            legend.setItemFont(new Font("SansSerif", Font.PLAIN, 12));
            legend.setBorder(0, 0, 0, 0);
            legend.setBackgroundPaint(Color.WHITE);
        }

        PiePlot<?> plot = (PiePlot<?>) chart.getPlot();
        plot.setBackgroundPaint(COLOR_BG_LIGHT);
        plot.setOutlineVisible(false);
        plot.setLabelFont(new Font("SansSerif", Font.BOLD, 11));
        plot.setLabelBackgroundPaint(Color.WHITE);
        plot.setLabelOutlinePaint(new Color(226, 232, 240));
        plot.setLabelShadowPaint(null);

        plot.setSectionPaint("Bugs Nuevos (OK)", COLOR_PRIMARY);
        plot.setSectionPaint("Reinyecciones 🚨", COLOR_DANGER);
    }

    private byte[] renderHdChart(JFreeChart chart, int width, int height) throws Exception {
        // Habilitar Antialiasing HD para texto y bordes curvos suaves
        chart.getRenderingHints().put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        chart.getRenderingHints().put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        return ChartUtils.encodeAsPNG(chart.createBufferedImage(width, height));
    }

    private void addSlideHeader(XSLFSlide slide, String title) {
        XSLFTextBox headerBox = slide.createTextBox();
        headerBox.setAnchor(new Rectangle(40, 30, 640, 50));
        XSLFTextParagraph p = headerBox.addNewTextParagraph();
        XSLFTextRun r = p.addNewTextRun();
        r.setText(title);
        r.setFontSize(20.0);
        r.setBold(true);
        r.setFontColor(COLOR_PRIMARY);
    }

    private void addBulletPoint(XSLFTextBox box, String boldPrefix, String text) {
        XSLFTextParagraph p = box.addNewTextParagraph();
        p.setSpaceBefore(10.0);
        XSLFTextRun r1 = p.addNewTextRun();
        r1.setText(boldPrefix);
        r1.setBold(true);
        r1.setFontSize(14.0);
        r1.setFontColor(COLOR_TEXT_DARK);

        XSLFTextRun r2 = p.addNewTextRun();
        r2.setText(text);
        r2.setFontSize(14.0);
        r2.setFontColor(new Color(71, 85, 105));
    }
}
