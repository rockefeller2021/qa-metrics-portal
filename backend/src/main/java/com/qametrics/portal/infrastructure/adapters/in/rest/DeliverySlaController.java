package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.model.DeliverySla;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.SlaStatus;
import com.qametrics.portal.domain.port.inbound.DeliverySlaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Adaptador REST para el Módulo de Seguimiento de Entregas y Gobierno SLA.
 */
@RestController
@RequestMapping("/deliveries")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Entregas SLA", description = "Gestión y trazabilidad de entregas, hitos y desviaciones SLA")
public class DeliverySlaController {

    private final DeliverySlaUseCase useCase;

    public DeliverySlaController(DeliverySlaUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @Operation(summary = "Listar entregas SLA", description = "Filtra por tipo de proyecto, estatus SLA y sprint/PI")
    public ResponseEntity<List<DeliverySla>> findAll(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) SlaStatus status,
            @RequestParam(required = false) String sprintOrPi,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(useCase.findAll(projectType, status, sprintOrPi, year, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliverySla> findById(@PathVariable Long id) {
        return useCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Registrar hito de entrega SLA", description = "Calcula automáticamente el estatus y días de retraso")
    public ResponseEntity<DeliverySla> create(@RequestBody DeliverySla deliverySla) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.create(deliverySla));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeliverySla> update(@PathVariable Long id, @RequestBody DeliverySla deliverySla) {
        return ResponseEntity.ok(useCase.update(id, deliverySla));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar hitos SLA en lote — solo ADMIN")
    public ResponseEntity<Void> deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request.all()) {
            useCase.deleteAll();
        } else if (request.ids() != null && !request.ids().isEmpty()) {
            useCase.deleteByIds(request.ids());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Resumen de cumplimiento SLA", description = "Retorna métricas consolidadas de entregas a tiempo y retrasadas")
    public ResponseEntity<Map<String, Object>> getDeliverySummary(
            @RequestParam(required = false) ProjectType projectType) {
        return ResponseEntity.ok(useCase.getDeliverySummary(projectType));
    }
}
