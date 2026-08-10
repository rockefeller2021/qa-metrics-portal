package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.User;
import com.qametrics.portal.domain.model.UserRole;
import com.qametrics.portal.domain.port.outbound.UserRepository;
import com.qametrics.portal.infrastructure.config.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias del caso de uso de Autenticación.
 */
@ExtendWith(MockitoExtension.class)
class AuthUseCaseImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthUseCaseImpl authUseCase;

    private User activeAdmin;

    @BeforeEach
    void setUp() {
        activeAdmin = new User(1L, "admin_qa", "admin@qaportal.com",
                "$2a$12$hash", UserRole.ADMIN, true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Login exitoso debe retornar token JWT")
    void shouldReturnTokenOnSuccessfulLogin() {
        // Given
        when(userRepository.findByUsername("admin_qa")).thenReturn(Optional.of(activeAdmin));
        when(passwordEncoder.matches("Admin1234!", "$2a$12$hash")).thenReturn(true);
        when(jwtService.generateToken(activeAdmin)).thenReturn("jwt.token.mock");

        // When
        String token = authUseCase.login("admin_qa", "Admin1234!");

        // Then
        assertEquals("jwt.token.mock", token);
        verify(jwtService).generateToken(activeAdmin);
    }

    @Test
    @DisplayName("Login con credenciales inválidas debe lanzar BadCredentialsException")
    void shouldThrowBadCredentialsOnWrongPassword() {
        // Given
        when(userRepository.findByUsername("admin_qa")).thenReturn(Optional.of(activeAdmin));
        when(passwordEncoder.matches("WrongPass!", "$2a$12$hash")).thenReturn(false);

        // When / Then
        assertThrows(BadCredentialsException.class,
                () -> authUseCase.login("admin_qa", "WrongPass!"));
        verify(jwtService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Login con usuario inexistente debe lanzar BadCredentialsException")
    void shouldThrowBadCredentialsOnUnknownUser() {
        // Given
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        // When / Then
        assertThrows(BadCredentialsException.class,
                () -> authUseCase.login("unknown", "pass"));
    }

    @Test
    @DisplayName("Login con usuario inactivo debe lanzar DisabledException")
    void shouldThrowDisabledExceptionForInactiveUser() {
        // Given
        User inactiveUser = new User(2L, "analyst_baja", "baja@qaportal.com",
                "$2a$12$hash", UserRole.ANALYST, false, LocalDateTime.now());
        when(userRepository.findByUsername("analyst_baja")).thenReturn(Optional.of(inactiveUser));

        // When / Then
        assertThrows(DisabledException.class,
                () -> authUseCase.login("analyst_baja", "pass"));
        verify(passwordEncoder, never()).matches(any(), any());
    }
}
