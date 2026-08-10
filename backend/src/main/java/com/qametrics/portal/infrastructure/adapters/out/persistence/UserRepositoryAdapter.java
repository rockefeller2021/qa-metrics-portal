package com.qametrics.portal.infrastructure.adapters.out.persistence;

import com.qametrics.portal.domain.model.User;
import com.qametrics.portal.domain.model.UserRole;
import com.qametrics.portal.domain.port.outbound.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador que implementa el puerto UserRepository del dominio
 * usando Spring Data JPA — es el puente entre dominio e infraestructura.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public java.util.List<User> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    // ── Mapeo Domain ↔ Entity ────────────────────────────────
    private User toDomain(UserEntity entity) {
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                UserRole.fromValue(entity.getRole()),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        if (user.getId() != null) entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        String roleStr = user.getRole() != null ? user.getRole().name().replace("ROLE_", "") : "ANALYST";
        entity.setRole(roleStr);
        entity.setActive(user.isActive());
        return entity;
    }
}
