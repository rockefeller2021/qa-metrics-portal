package com.qametrics.portal.domain.port.inbound;

import com.qametrics.portal.domain.model.User;

/**
 * Puerto de entrada (Inbound Port) para Autenticación.
 * Define el contrato de la lógica de negocio de Auth.
 */
public interface AuthUseCase {

    /**
     * Autentica un usuario y retorna un token JWT.
     * @param username nombre de usuario
     * @param password contraseña en texto plano
     * @return token JWT firmado
     */
    String login(String username, String password);

    /**
     * Valida un token JWT y retorna el usuario asociado.
     * @param token JWT token
     * @return usuario autenticado
     */
    User validateToken(String token);

    /**
     * Refresca un token válido próximo a expirar.
     * @param token token vigente
     * @return nuevo token con expiración renovada
     */
    String refreshToken(String token);
}
