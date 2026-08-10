package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.port.inbound.BulkImportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Adaptador REST para la importación masiva de datos y descarga de plantillas muestrales.
 */
@RestController
@RequestMapping("/import")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Importación Masiva Excel/CSV", description = "Carga masiva de ejecuciones, bugs y entregas SLA")
public class BulkImportController {

    private final BulkImportUseCase bulkImportUseCase;

    public BulkImportController(BulkImportUseCase bulkImportUseCase) {
        this.bulkImportUseCase = bulkImportUseCase;
    }

    @PostMapping("/executions")
    @Operation(summary = "Importar ejecuciones masivamente", description = "Carga ejecuciones de prueba desde archivo Excel (.xlsx) o CSV")
    public ResponseEntity<Map<String, Object>> importExecutions(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bulkImportUseCase.importExecutions(file));
    }

    @PostMapping("/bugs")
    @Operation(summary = "Importar bugs masivamente", description = "Carga incidencias del BugTracker desde archivo Excel (.xlsx)")
    public ResponseEntity<Map<String, Object>> importBugs(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bulkImportUseCase.importBugs(file));
    }

    @PostMapping("/deliveries")
    @Operation(summary = "Importar hitos SLA masivamente", description = "Carga hitos de entregas SLA desde archivo Excel (.xlsx)")
    public ResponseEntity<Map<String, Object>> importDeliveries(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(bulkImportUseCase.importDeliveries(file));
    }

    @GetMapping("/template/{type}")
    @Operation(summary = "Descargar plantilla muestral de Excel", description = "Descarga una plantilla .xlsx vacía con encabezados requeridos")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String type) {
        byte[] bytes = bulkImportUseCase.generateSampleTemplate(type);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Plantilla_Importacion_" + type + ".xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }
}
