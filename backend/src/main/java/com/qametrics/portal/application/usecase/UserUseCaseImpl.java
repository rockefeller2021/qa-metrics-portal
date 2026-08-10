package com.qametrics.portal.application.usecase;

import com.qametrics.portal.domain.model.User;
import com.qametrics.portal.domain.model.UserRole;
import com.qametrics.portal.domain.port.inbound.UserUseCase;
import com.qametrics.portal.domain.port.outbound.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Caso de Uso — Gestión de Usuarios, Roles RBAC y Encriptación BCrypt.
 */
@Service
@Transactional
public class UserUseCaseImpl implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserUseCaseImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User createUser(String username, String email, String password, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("El nombre de usuario '" + username + "' ya existe.");
        }

        String hash = passwordEncoder.encode(password);
        User user = new User(
                null,
                username.trim().toLowerCase(),
                (email != null) ? email.trim() : username.trim() + "@qametrics.com",
                hash,
                (role != null) ? role : UserRole.ANALYST,
                true,
                LocalDateTime.now()
        );

        return userRepository.save(user);
    }

    @Override
    public User toggleUserStatus(Long id, boolean active) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        user.setActive(active);
        return userRepository.save(user);
    }

    @Override
    public User updatePassword(Long id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
