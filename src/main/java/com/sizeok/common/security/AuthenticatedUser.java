package com.sizeok.common.security;

public record AuthenticatedUser(
        String userId,
        String email,
        String role
) {
    public boolean isAdmin() {
        return "SUPER_ADMIN".equalsIgnoreCase(role)
                || "CATALOG_ADMIN".equalsIgnoreCase(role)
                || "DATA_REVIEWER".equalsIgnoreCase(role)
                || "SUPPORT".equalsIgnoreCase(role);
    }
}
