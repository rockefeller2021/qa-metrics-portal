package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.User;
import com.qametrics.portal.domain.port.inbound.AuthUseCase;
import com.qametrics.portal.domain.port.outbound.UserRepository;
import com.qametrics.portal.infrastructure.config.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Implementación del caso de uso de Autenticación.
 * Orquesta: buscar usuario → validar BCrypt → generar JWT.
 */
@Service
public class AuthUseCaseImpl implements AuthUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthUseCaseImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public String login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!user.isActive()) {
            throw new DisabledException("Usuario inactivo. Contacte al administrador.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        return jwtService.generateToken(user);
    }

    @Override
    public User validateToken(String token) {
        String username = jwtService.extractUsername(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Token inválido"));
    }

    @Override
    public String refreshToken(String token) {
        if (!jwtService.isTokenValid(token)) {
            throw new BadCredentialsException("Token expirado o inválido");
        }
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado"));
        return jwtService.generateToken(user);
    }
}
