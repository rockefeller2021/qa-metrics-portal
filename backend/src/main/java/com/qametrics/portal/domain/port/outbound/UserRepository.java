package com.qametrics.portal.domain.port.outbound;

import com.qametrics.portal.domain.model.User;
import java.util.Optional;

/**
 * Puerto de salida (Outbound Port) para persistencia de usuarios.
 * El dominio solo conoce esta interfaz — nunca la implementación JPA.
 */
public interface UserRepository {
    java.util.List<User> findAll();
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    User save(User user);
    void deleteById(Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
