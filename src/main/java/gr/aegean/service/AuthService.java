package gr.aegean.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import gr.aegean.model.dto.auth.AuthResponse;
import gr.aegean.model.dto.auth.RegisterRequest;
import gr.aegean.entity.User;
import gr.aegean.model.user.UserPrincipal;
import gr.aegean.model.dto.auth.LoginRequest;
import gr.aegean.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * @return an AuthResponse containing the JWT token.
     */
    public AuthResponse registerUser(RegisterRequest request) {
        User user = new User(
                request.firstname(),
                request.lastname(),
                request.email(),
                request.password(),
                request.role());

        userService.validateUser(user);
        user.setPassword(passwordEncoder.encode(request.password()));
        userService.registerUser(user);

        UserPrincipal userPrincipal = new UserPrincipal(user);
        String jwtToken = jwtService.assignToken(userPrincipal);

        return new AuthResponse(jwtToken);
    }

    /**
     * @return an AuthResponse containing the JWT.
     */
    public AuthResponse loginUser(LoginRequest request) {
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException bce) {
            throw new UnauthorizedException("Username or password is incorrect");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwtToken = jwtService.assignToken(userPrincipal);

        return new AuthResponse(jwtToken);
    }

}
