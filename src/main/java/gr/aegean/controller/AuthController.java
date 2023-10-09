package gr.aegean.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gr.aegean.model.dto.auth.AuthResponse;
import gr.aegean.service.AuthService;
import gr.aegean.model.dto.auth.RegisterRequest;
import gr.aegean.model.dto.auth.LoginRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * @return a ResponseEntity containing the authentication token.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.registerUser(request);

        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    /**
     * @return a ResponseEntity containing the authentication token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.loginUser(request);

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }
}
