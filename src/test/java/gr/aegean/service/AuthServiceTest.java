package gr.aegean.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gr.aegean.entity.User;
import gr.aegean.model.user.UserPrincipal;
import gr.aegean.model.user.UserRole;
import gr.aegean.model.dto.auth.AuthResponse;
import gr.aegean.model.dto.auth.RegisterRequest;
import gr.aegean.model.dto.auth.LoginRequest;
import gr.aegean.exception.UnauthorizedException;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    private AuthService underTest;

    @BeforeEach
    void setup() {
        underTest = new AuthService(userService, passwordEncoder, jwtService, authenticationManager);
    }

    @Test
    void shouldRegisterUserAndReturnJwtToken() {
        //Arrange
        RegisterRequest request = new RegisterRequest(
                "test",
                "test",
                "test@gmail.com",
                "3frMH4v!20d4",
                UserRole.ROLE_CUSTOMER);

        User user = new User(
                request.firstname(),
                request.lastname(),
                request.email(),
                request.password(),
                request.role());

        String jwtToken = "jwtToken";

        when(passwordEncoder.encode(user.getPassword())).thenReturn("hashedPassword");
        when(jwtService.assignToken(any(UserPrincipal.class))).thenReturn(jwtToken);
        doNothing().when(userService).registerUser(any(User.class));

        //Act
        AuthResponse authResponse = underTest.registerUser(request);

        //Assert
        assertThat(authResponse.token()).isEqualTo(jwtToken);

        verify(userService, times(1)).registerUser(any(User.class));
    }

    /*
        The principal(1st argument) of the UsernamePasswordAuthenticationToken is of type SecurityUser, because
        SecurityUser implements UserDetails.
     */
    @Test
    void shouldLoginUserAndReturnTheJWT() {
        //Arrange
        LoginRequest loginRequest = new LoginRequest("test@gmail.com", "test");
        User user = new User(loginRequest.email(), loginRequest.password());
        String jwtToken = "jwtToken";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
                new UsernamePasswordAuthenticationToken(new UserPrincipal(user), null));
        when(jwtService.assignToken(any(UserPrincipal.class))).thenReturn(jwtToken);

        //Act
        AuthResponse actual = underTest.loginUser(loginRequest);

        //Assert
        assertThat(actual.token()).isEqualTo(jwtToken);
    }

    /*
        When authenticate from authentication manager fails it will throw either spring.security.BadCredentialsException
        if password is wrong or UsernameNotFoundException if the user's email doesn't exist. The
        UsernameNotFoundException is caught by Spring and a spring.security.BadCredentialsException is thrown instead.
     */
    @Test
    void shouldThrowUnauthorizedExceptionWhenLoginEmailOrPasswordIsWrong() {
        //Arrange
        LoginRequest request = new LoginRequest("test@example.com", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Username or password is incorrect"));

        //Act Assert
        assertThatThrownBy(() -> underTest.loginUser(request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Username or password is incorrect");
    }
}
