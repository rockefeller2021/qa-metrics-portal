package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.model.Bug;
import com.qametrics.portal.domain.model.ProjectType;
import com.qametrics.portal.domain.port.inbound.BugUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Adaptador REST para el BugTracker.
 */
@RestController
@RequestMapping("/bugs")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "BugTracker", description = "Gestión de bugs con detección de reinyecciones")
public class BugController {

    private final BugUseCase useCase;

    public BugController(BugUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @Operation(summary = "Listar bugs", description = "Filtra por tipo de proyecto y sprint/PI")
    public ResponseEntity<List<Bug>> findAll(
            @RequestParam(required = false) ProjectType projectType,
            @RequestParam(required = false) String sprintOrPi,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseEntity.ok(useCase.findAll(projectType, sprintOrPi, year, month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Bug> findById(@PathVariable Long id) {
        return useCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Registrar bug", description = "Detecta automáticamente si es reinyección")
    public ResponseEntity<Bug> create(@RequestBody Bug bug) {
        return ResponseEntity.status(HttpStatus.CREATED).body(useCase.create(bug));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Bug> update(@PathVariable Long id, @RequestBody Bug bug) {
        return ResponseEntity.ok(useCase.update(id, bug));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        useCase.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar bugs en lote — solo ADMIN", description = "Elimina todos o los IDs especificados")
    public ResponseEntity<Void> deleteBatch(@RequestBody BatchDeleteRequest request) {
        if (request.all()) {
            useCase.deleteAll();
        } else if (request.ids() != null && !request.ids().isEmpty()) {
            useCase.deleteByIds(request.ids());
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reinjections")
    @Operation(summary = "Listar solo bugs con reinyección detectada")
    public ResponseEntity<List<Bug>> findReinjections(
            @RequestParam(required = false) ProjectType projectType) {
        return ResponseEntity.ok(useCase.findReinjections(projectType));
    }
}
