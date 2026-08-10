package com.qametrics.portal.domain.port.inbound;

import com.qametrics.portal.domain.model.User;
import com.qametrics.portal.domain.model.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada (Inbound Port) para la gestión de usuarios y roles RBAC.
 */
public interface UserUseCase {

    List<User> findAll();

    Optional<User> findById(Long id);

    User createUser(String username, String email, String password, UserRole role);

    User toggleUserStatus(Long id, boolean active);

    User updatePassword(Long id, String newPassword);

    void deleteUser(Long id);
}
