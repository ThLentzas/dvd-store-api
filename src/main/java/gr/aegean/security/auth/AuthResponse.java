package gr.aegean.security.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    @JsonIgnore
    private Integer id;

    public AuthResponse(String token) {
        this.token = token;
    }
}
