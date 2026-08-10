package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.model.ExecutionRun;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.model.TestExecution;
import com.qametrics.portal.domain.port.inbound.TestExecutionUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adaptador REST para Ejecuciones de Prueba y Retests N-iterativos.
 */
@RestController
@RequestMapping("/executions")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ejecuciones", description = "Gestión de ejecuciones de prueba y retests iterativos")
public class TestExecutionController {

    private final TestExecutionUseCase useCase;

    public TestExecutionController(TestExecutionUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @Operation(summary = "Listar ejecuciones", description = "Filtra por tipo de proyecto y sprint")
    public ResponseEntity<List<TestExecution>> findAll(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) String sprintOrPi,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(useCase.findAll(projectType, sprintOrPi, year, month));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener ejecución por ID")
    public ResponseEntity<TestExecution> findById(@PathVariable Long id) {
        return useCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nueva ejecución de prueba")
    public ResponseEntity<TestExecution> create(@RequestBody TestExecution execution) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.create(execution));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar ejecución existente")
    public ResponseEntity<TestExecution> update(@PathVariable Long id,
                                                 @RequestBody TestExecution execution) {
        return ResponseEntity.ok(useCase.update(id, execution));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar ejecución — solo ADMIN")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar ejecuciones en lote — solo ADMIN")
    public ResponseEntity<Void> deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request.all()) {
            useCase.deleteAll();
        } else if (request.ids() != null && !request.ids().isEmpty()) {
            useCase.deleteByIds(request.ids());
        }
        return ResponseEntity.noContent().build();
    }

    // ── Retests ───────────────────────────────────────────────
    @GetMapping("/{id}/runs")
    @Operation(summary = "Obtener todas las iteraciones de una ejecución")
    public ResponseEntity<List<ExecutionRun>> getRuns(@PathVariable Long id) {
        return ResponseEntity.ok(useCase.findRunsByExecutionId(id));
    }

    @PostMapping("/{id}/runs")
    @Operation(summary = "Agregar nuevo Retest a una ejecución existente")
    public ResponseEntity<ExecutionRun> addRun(@PathVariable Long id,
                                                @RequestBody ExecutionRun run) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.addRun(id, run));
    }
}
