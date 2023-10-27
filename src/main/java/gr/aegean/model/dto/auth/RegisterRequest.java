package gr.aegean.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import gr.aegean.model.user.UserRole;


public record RegisterRequest(
        @NotBlank(message = "The First Name field is required")
        String firstname,
        @NotBlank(message = "The Last Name field is required")
        String lastname,
        @NotBlank(message = "The Email field is required")
        String email,
        @NotBlank(message = "The Password field is required")
        String password,
        @NotNull(message = "The Role field is required")
        UserRole role) {

}
