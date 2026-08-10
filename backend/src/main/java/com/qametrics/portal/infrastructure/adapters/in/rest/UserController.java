package com.qametrics.portal.infrastructure.adapters.in.rest;

import com.qametrics.portal.domain.model.User;
import com.qametrics.portal.domain.model.UserRole;
import com.qametrics.portal.domain.port.inbound.UserUseCase;
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
 * Adaptador REST para la Gestión de Usuarios y Roles RBAC.
 * Resguardado con Spring Security (@PreAuthorize hasRole('ADMIN')).
 */
@RestController
@RequestMapping("/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios y Roles RBAC", description = "Administración de usuarios, roles y control de acceso")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios", description = "Retorna la lista de usuarios registrados con sus roles")
    public ResponseEntity<List<User>> findAll() {
        return ResponseEntity.ok(userUseCase.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> findById(@PathVariable Long id) {
        return userUseCase.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear nuevo usuario", description = "Registra un usuario con encriptación BCrypt y rol asignado")
    public ResponseEntity<User> create(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String email    = payload.get("email");
        String password = payload.get("password");
        String roleStr  = payload.get("role");

        UserRole role = UserRole.fromValue(roleStr);

        User created = userUseCase.createUser(username, email, password, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Activar o Inactivar usuario")
    public ResponseEntity<User> toggleStatus(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        Boolean active = payload.getOrDefault("active", true);
        return ResponseEntity.ok(userUseCase.toggleUserStatus(id, active));
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "Restablecer contraseña de usuario")
    public ResponseEntity<User> updatePassword(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("password");
        return ResponseEntity.ok(userUseCase.updatePassword(id, newPassword));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userUseCase.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
