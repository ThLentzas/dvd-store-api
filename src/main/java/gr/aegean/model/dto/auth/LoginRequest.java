package gr.aegean.model.dto.auth;

import jakarta.validation.constraints.NotBlank;


public record LoginRequest(
        @NotBlank(message = "The Email field is necessary")
        String email,
        @NotBlank(message = "The Password field is necessary")
        String password) {
}
