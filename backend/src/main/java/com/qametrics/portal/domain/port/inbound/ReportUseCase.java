package com.qametrics.portal.domain.port.inbound;

/**
 * Puerto de entrada (Inbound Port) para la generación de reportes ejecutivos multi-formato.
 */
public interface ReportUseCase {

    byte[] generatePdfReport(String projectType, Integer year, Integer month, String developerName, String designerAnalyst);

    byte[] generateExcelReport(String projectType, Integer year, Integer month, String developerName, String designerAnalyst);

    byte[] generatePptxReport(String projectType, Integer year, Integer month, String developerName, String designerAnalyst);
}
