package com.sizeok.common.security;

import com.sizeok.common.exception.AccessDeniedOperationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public AuthenticatedUser requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new AccessDeniedOperationException("Authentication required");
        }
        return user;
    }

    public AuthenticatedUser requireAdminUser() {
        AuthenticatedUser user = requireCurrentUser();
        if (!user.isAdmin()) {
            throw new AccessDeniedOperationException("Admin role required for this operation");
        }
        return user;
    }
}
