package gr.aegean.service;


import gr.aegean.exception.BadCredentialsException;
import gr.aegean.security.auth.AuthRequest;
import gr.aegean.security.auth.AuthResponse;
import gr.aegean.security.auth.RegisterRequest;
import gr.aegean.model.user.User;
import gr.aegean.model.user.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.apache.commons.validator.routines.EmailValidator;
import org.passay.CharacterRule;
import org.passay.EnglishCharacterData;
import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.RuleResult;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


/**
 * Service class for handling user's authentication and registration by using JWT as authentication mechanism.
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user with the given information.
     * If the registration is successful, all the user credentials are valid, the password is hashed,a JWT token is
     * assigned and the user is registered in the database via the UserService.
     *
     * @param request the user's registration information.
     * @return an AuthResponse containing the JWT token and the registered user ID for the URI.
     */
    public AuthResponse register(RegisterRequest request) {
        if(!isRegisterRequestValid(request)) {
            throw new BadCredentialsException("All fields are necessary");
        }

        User user = new User(request.firstname(), request.lastname(), request.email(),
                request.password(), request.role());

        String jwtToken = null;
        Integer id = null;

        if (isUserValid(user)) {
            user.setPassword(encoder.encode(user.getPassword()));
            UserPrincipal userPrincipal = new UserPrincipal(user);

            id = userService.registerUser(user);
            jwtToken = jwtService.assignToken(userPrincipal);
        }

        return new AuthResponse(jwtToken, id);
    }

    /**
     * Authenticates a user with the provided username and password. In our case it's the user email.
     * If the registration is successful, a JWT token is assigned.
     *
     * @param request the user's authentication information.
     * @return an AuthResponse containing the JWT.
     */
    public AuthResponse authenticate(AuthRequest request) {
        if(!isAuthRequestValid(request)) {
            throw new BadCredentialsException("All fields are necessary");
        }

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getEmail(),
                    request.getPassword()));
        } catch (Exception e) {
            throw new BadCredentialsException("Username or password is incorrect");
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwtToken = jwtService.assignToken(userPrincipal);

        return new AuthResponse(jwtToken);
    }

    /**
     * Validates the user, by verifying that it contains a valid name, email, and password.
     *
     * @param user the user to be validated.
     * @return true if all the user's credentials are valid.
     * @throws BadCredentialsException if any of these fields are missing or invalid.
     */
    private boolean isUserValid(User user) {
        boolean isValidName = isNameValid(user.getFirstname(), user.getLastname());
        boolean isValidEmail = isEmailValid(user.getEmail());
        boolean isValidPassword = isPasswordValid(user.getPassword());

        return isValidName && isValidEmail && isValidPassword;
    }

    /**
     * Validates the user's firstname or the user's lastname, by verifying that they only contain letters.
     *
     * @param firstname the user's firstname.
     * @param lastname  the user's lastname.
     * @return true if both firstname and lastname are valid.
     * @throws BadCredentialsException when either the user's name of the user's lastname is null,
     *                                 empty or contains non-letter characters.
     */
    private boolean isNameValid(String firstname, String lastname) {
        if (!firstname.matches("^[a-zA-Z]*$")) {
            throw new BadCredentialsException("Invalid firstname. Name should contain only characters");
        }

        if (!lastname.matches("^[a-zA-Z]*$")) {
            throw new BadCredentialsException("Invalid lastname. Name should contain only characters");
        }

        return true;
    }

    /**
     * Validates the user's email, by verifying that it matches the email pattern.
     *
     * @param email the user's email.
     * @return true if the email is valid.
     * @throws BadCredentialsException if the email is null, empty, or does not match the email pattern.
     */
    private boolean isEmailValid(String email) {
        EmailValidator validator = EmailValidator.getInstance();
        if (!validator.isValid(email)) {
            throw new BadCredentialsException("Invalid email");
        }

        return true;
    }

    /**
     * Validates that the user's password meets the specified requirements.
     *
     * @param password the password to be validated.
     * @return true if the password is valid.
     * @throws BadCredentialsException if the password is null, empty, or does not meet the specified
     *                                 requirements.
     */
    private boolean isPasswordValid(String password) {
        PasswordValidator validator = new PasswordValidator(
                new LengthRule(8, 30),
                new CharacterRule(EnglishCharacterData.UpperCase, 1),
                new CharacterRule(EnglishCharacterData.LowerCase, 1),
                new CharacterRule(EnglishCharacterData.Digit, 1),
                new CharacterRule(EnglishCharacterData.Special, 1)
        );

        RuleResult result = validator.validate(new PasswordData(password));
        if (!result.isValid()) {
            throw new BadCredentialsException(validator.getMessages(result).get(0));
        }

        return true;
    }

    private boolean isRegisterRequestValid(RegisterRequest request) {
       return  request.firstname() != null && !request.firstname().isEmpty()
               && request.lastname() != null && !request.lastname().isEmpty()
               && request.email() != null && !request.email().isEmpty()
               && request.password() != null && !request.password().isEmpty()
               && request.role() != null;
    }

    private boolean isAuthRequestValid(AuthRequest request) {
        return  request.getEmail() != null && !request.getEmail().isEmpty()
                && request.getPassword() != null && !request.getPassword().isEmpty();
    }
}
