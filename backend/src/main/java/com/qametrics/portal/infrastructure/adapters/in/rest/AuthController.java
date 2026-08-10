package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.port.inbound.AuthUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Adaptador REST de entrada para Autenticación.
 * Expone los endpoints públicos de login y refresh.
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Endpoints de login y gestión de tokens JWT")
public class AuthController {

    private final AuthUseCase authUseCase;

    public AuthController(AuthUseCase authUseCase) {
        this.authUseCase = authUseCase;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica con username/password y retorna JWT")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        String token = authUseCase.login(request.username(), request.password());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "type", "Bearer",
                "message", "Login exitoso"
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar token", description = "Renueva un token JWT válido")
    public ResponseEntity<Map<String, String>> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String newToken = authUseCase.refreshToken(token);
        return ResponseEntity.ok(Map.of("token", newToken, "type", "Bearer"));
    }

    // ── DTOs de Request ──────────────────────────────────────
    public record LoginRequest(
            @NotBlank(message = "El username es obligatorio") String username,
            @NotBlank(message = "La contraseña es obligatoria") String password
    ) {}
}
