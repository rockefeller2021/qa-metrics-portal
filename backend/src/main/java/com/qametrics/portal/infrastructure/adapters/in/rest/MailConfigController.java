package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.application.service.MailConfigService;
import com.qametrics.portal.domain.model.MailConfigDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Adaptador REST para la Configuración Dinámica de Servidor SMTP y Probador de Conexión de Alertas.
 */
@RestController
@RequestMapping("/mail")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Configuración Alertas SMTP", description = "Gestión de servidor de correo y probador de conexión en tiempo real")
public class MailConfigController {

    private final MailConfigService mailConfigService;

    public MailConfigController(MailConfigService mailConfigService) {
        this.mailConfigService = mailConfigService;
    }

    @GetMapping("/config")
    @Operation(summary = "Obtener configuración SMTP actual")
    public ResponseEntity<MailConfigDto> getConfig() {
        return ResponseEntity.ok(mailConfigService.getConfig());
    }

    @PostMapping("/config")
    @Operation(summary = "Guardar configuración de servidor SMTP y destinatarios de alertas")
    public ResponseEntity<MailConfigDto> updateConfig(@RequestBody MailConfigDto dto) {
        return ResponseEntity.ok(mailConfigService.updateConfig(dto));
    }

    @PostMapping("/test")
    @Operation(summary = "Probar conexión de envío de correo SMTP", description = "Envía un mensaje HTML de prueba en vivo a la casilla especificada")
    public ResponseEntity<Map<String, Object>> testEmail(@RequestParam(required = false) String recipient) {
        return ResponseEntity.ok(mailConfigService.sendTestEmail(recipient));
    }
}
