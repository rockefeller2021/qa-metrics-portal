package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.port.inbound.ReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Adaptador REST para la exportación de reportes ejecutivos en PDF, Excel (.xlsx) y PowerPoint (.pptx).
 */
@RestController
@RequestMapping("/reports")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes Ejecutivos", description = "Endpoints de descarga de informes en PDF, XLSX y PPTX")
public class ReportController {

    private final ReportUseCase reportUseCase;

    public ReportController(ReportUseCase reportUseCase) {
        this.reportUseCase = reportUseCase;
    }

    @GetMapping("/pdf")
    @Operation(summary = "Exportar Informe PDF", description = "Genera un documento PDF formal con métricas consolidadas")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String developerName,
            @RequestParam(required = false) String designerAnalyst) {
        byte[] pdf = reportUseCase.generatePdfReport(projectType, year, month, developerName, designerAnalyst);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=QA_Executive_Report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/excel")
    @Operation(summary = "Exportar Libro Excel (.xlsx)", description = "Genera un archivo Excel multi-pestaña con el histórico completo")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String developerName,
            @RequestParam(required = false) String designerAnalyst) {
        byte[] excel = reportUseCase.generateExcelReport(projectType, year, month, developerName, designerAnalyst);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=QA_Metrics_Consolidated.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }

    @GetMapping("/pptx")
    @Operation(summary = "Exportar Presentación PowerPoint (.pptx)", description = "Genera diapositivas ejecutivas automáticas para comités de QA")
    public ResponseEntity<byte[]> exportPptx(
            @RequestParam(required = false) String projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) String developerName,
            @RequestParam(required = false) String designerAnalyst) {
        byte[] pptx = reportUseCase.generatePptxReport(projectType, year, month, developerName, designerAnalyst);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=QA_Executive_Presentation.pptx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation"))
                .body(pptx);
    }
}
