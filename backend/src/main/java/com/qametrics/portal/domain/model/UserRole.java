package com.qametrics.portal.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum de roles del sistema — Soporta prefijo ROLE_ para Spring Security y Frontend.
 */
public enum UserRole {
    ROLE_ADMIN,
    ROLE_ANALYST,
    ADMIN,
    ANALYST;

    @JsonValue
    public String toValue() {
        if (this == ADMIN || this == ROLE_ADMIN) return "ROLE_ADMIN";
        return "ROLE_ANALYST";
    }

    @JsonCreator
    public static UserRole fromValue(String value) {
        if (value == null) return ROLE_ANALYST;
        String val = value.trim().toUpperCase();
        if (val.contains("ADMIN")) return ROLE_ADMIN;
        return ROLE_ANALYST;
    }
}
