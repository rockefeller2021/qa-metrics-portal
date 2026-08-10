package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.model.*;
import com.qametrics.portal.domain.port.inbound.ClientTrackingUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/client-tracking")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "SeguimientoCliente", description = "Módulo de seguimiento de entregas y devoluciones de cliente (Target 95%)")
public class ClientTrackingController {

    private final ClientTrackingUseCase useCase;

    public ClientTrackingController(ClientTrackingUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping("/metrics")
    @Operation(summary = "Listar entregas de cliente")
    public ResponseEntity<List<ClientDeliveryMetric>> findAllMetrics(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(useCase.findAllMetrics(projectType, year, month));
    }

    @PostMapping("/metrics")
    @Operation(summary = "Registrar entregas de cliente (Evolutivos, Soportes, Standard Change)")
    public ResponseEntity<ClientDeliveryMetric> createMetric(@RequestBody ClientDeliveryMetric metric) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createMetric(metric));
    }

    @PutMapping("/metrics/{id}")
    @Operation(summary = "Actualizar entregas de cliente")
    public ResponseEntity<ClientDeliveryMetric> updateMetric(
            @PathVariable Long id,
            @RequestBody ClientDeliveryMetric metric) {
        return ResponseEntity.ok(useCase.updateMetric(id, metric));
    }

    @DeleteMapping("/metrics/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar entregas de cliente")
    public ResponseEntity<Void> deleteMetric(@PathVariable Long id) {
        useCase.deleteMetric(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/returns")
    @Operation(summary = "Listar devoluciones del cliente")
    public ResponseEntity<List<ClientReturn>> findAllReturns(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(useCase.findAllReturns(projectType, year, month));
    }

    @PostMapping("/returns")
    @Operation(summary = "Registrar devolución de cliente por IBL", description = "Aplica la regla de negocio: 1ª vez NO cuenta en calidad, 2ª vez en adelante SÍ cuenta.")
    public ResponseEntity<ClientReturn> createReturn(@RequestBody ClientReturn clientReturn) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.createReturn(clientReturn));
    }

    @DeleteMapping("/returns/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar devolución del cliente")
    public ResponseEntity<Void> deleteReturn(@PathVariable Long id) {
        useCase.deleteReturn(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtener resumen de porcentajes de calidad y tendencia mensual para gráficas (Target 95%)")
    public ResponseEntity<ClientTrackingSummary> getSummary(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(useCase.getSummary(projectType, year, month));
    }
}
