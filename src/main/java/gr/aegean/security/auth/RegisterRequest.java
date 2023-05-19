package gr.aegean.security.auth;

import gr.aegean.model.user.UserRole;

public record RegisterRequest(String firstname, String lastname, String email, String password, UserRole role) {}
