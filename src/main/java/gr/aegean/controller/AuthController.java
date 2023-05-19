package gr.aegean.controller;

import gr.aegean.security.auth.AuthRequest;
import gr.aegean.security.auth.AuthResponse;
import gr.aegean.service.AuthService;
import gr.aegean.security.auth.RegisterRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Controller class for handling authentication-related requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    /**
     * Registers a new user with the given details and generates an access token.
     *
     * @param request the request containing the user's registration details.
     * @param uriBuilder a builder for creating URIs.
     * @return a ResponseEntity containing the authentication token.
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        AuthResponse authResponse = authService.register(request);

        URI location = uriBuilder
                .path("/api/v1/users/{userID}")
                .buildAndExpand(authResponse.getId())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(authResponse, headers, HttpStatus.CREATED);
    }

    /**
     * Authenticates a user by checking the provided credentials and generates an access token.
     *
     * @param request the request containing the user's credentials
     * @return a ResponseEntity containing the authentication token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody AuthRequest request) {
        AuthResponse response = authService.authenticate(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
