package gr.aegean.service;

import gr.aegean.exception.BadCredentialsException;
import gr.aegean.model.user.User;
import gr.aegean.model.user.UserPrincipal;
import gr.aegean.model.user.UserRole;
import gr.aegean.security.auth.AuthRequest;
import gr.aegean.security.auth.AuthResponse;
import gr.aegean.security.auth.RegisterRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
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
        RegisterRequest request = new RegisterRequest("test", "test", "test@gmail.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);
        User user = new User(request.firstname(), request.lastname(), request.email(),
                request.password(), request.role());

        String jwtToken = "jwtToken";
        Integer generatedID = 1;

        when(passwordEncoder.encode(user.getPassword())).thenReturn("hashedPassword");
        when(userService.registerUser(any(User.class))).thenReturn(generatedID);
        when(jwtService.assignToken(any(UserPrincipal.class))).thenReturn(jwtToken);

        //Act
        AuthResponse authResponse = underTest.register(request);

        //Assert
        assertThat(authResponse.getId()).isEqualTo(generatedID);
        assertThat(authResponse.getToken()).isEqualTo(jwtToken);

        verify(passwordEncoder, times(1)).encode(user.getPassword());
        verify(userService, times(1)).registerUser(any(User.class));
        verify(jwtService, times(1)).assignToken(any(UserPrincipal.class));
    }

    @Test
    void shouldAuthenticateUserAndReturnJwtToken() {
        //Arrange
        AuthRequest authRequest = new AuthRequest("test@gmail.com", "test");
        UserPrincipal userPrincipal = new UserPrincipal(new User("test@gmail.com", "test",
                UserRole.ROLE_EMPLOYEE));

        String jwtToken = "jwtToken";

        when(jwtService.assignToken(any(UserPrincipal.class))).thenReturn(jwtToken);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
                new UsernamePasswordAuthenticationToken(userPrincipal,
                        "test", userPrincipal.getAuthorities()));

        //Act
        AuthResponse authResponse = underTest.authenticate(authRequest);

        //Assert
        assertThat(authResponse.getToken()).isEqualTo(jwtToken);

        verify(jwtService, times(1)).assignToken(any(UserPrincipal.class));
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenAuthEmailIsNull() {
        //Arrange
        AuthRequest request = new AuthRequest(null, "password");

        //Act Assert
        assertThatThrownBy(() -> underTest.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenAuthEmailIsEmpty() {
        //Arrange
        AuthRequest request = new AuthRequest("", "password");

        //Act Assert
        assertThatThrownBy(() -> underTest.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenAuthPasswordIsNull() {
        //Arrange
        AuthRequest request = new AuthRequest("test@example.com", null);

        //Act Assert
        assertThatThrownBy(() -> underTest.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenAuthPasswordIsEmpty() {
        //Arrange
        AuthRequest request = new AuthRequest("test@example.com", "");

        //Act Assert
        assertThatThrownBy(() -> underTest.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    //When authenticate from authentication manager fails it will throw either spring.security.BadCredentialsException
    //if password is wrong or EmptyResultDataAccessException if the user's email doesn't exist. In any case both are run
    //time exceptions, we just throw a new RuntimeException to cover both cases.
    @Test
    void shouldThrowBadCredentialsExceptionWhenAuthEmailOrPasswordIsWrong() {
        //Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException());

        //Act
        AuthRequest request = new AuthRequest("test@example.com", "password");

        //Assert
        assertThatThrownBy(() -> underTest.authenticate(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Username or password is incorrect");

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterFirstnameIsNull() {
        //Arrange
        RegisterRequest request = new RegisterRequest(null, "test", "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterFirstnameIsEmpty() {
        //Arrange
        RegisterRequest request = new RegisterRequest("", "test", "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLastnameIsNull() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", null, "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLastnameIsEmpty() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "", "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterEmailIsNull() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "test", null,
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterEmailIsEmpty() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "test", "",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterPasswordIsNull() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "test", "test@example.com",
                null, UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterPasswordIsEmpty() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "test", "test@example.com",
                "", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterRoleIsNull() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "test", "test@example.com",
                "3frMH4v!20d4", null);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("All fields are necessary");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterFirstnameContainsNumbers() {
        //Arrange
        RegisterRequest request = new RegisterRequest("t4st", "test", "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid firstname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterFirstnameContainsSpecialCharacters() {
        //Arrange
        RegisterRequest request = new RegisterRequest("t$st", "test", "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid firstname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLastnameContainsNumbers() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "test9", "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid lastname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterLastnameContainsSpecialCharacters() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "te*t", "test@example.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid lastname. Name should contain only characters");
    }

    @Test
    void shouldThrowBadCredentialsExceptionWhenRegisterEmailIsInvalid() {
        //Arrange
        RegisterRequest request = new RegisterRequest("test", "test", "testexample.com",
                "3frMH4v!20d4", UserRole.ROLE_CUSTOMER);

        //Act Assert
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email");
    }
}
