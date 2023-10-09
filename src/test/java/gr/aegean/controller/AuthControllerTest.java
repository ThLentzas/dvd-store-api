package gr.aegean.controller;

import gr.aegean.config.DeserializerConfig;
import gr.aegean.config.security.AuthConfig;
import gr.aegean.config.security.JwtConfig;
import gr.aegean.config.security.SecurityConfig;
import gr.aegean.exception.CustomAccessDeniedHandler;
import gr.aegean.model.dto.auth.LoginRequest;
import gr.aegean.repository.UserRepository;
import gr.aegean.model.dto.auth.AuthResponse;
import gr.aegean.model.dto.auth.RegisterRequest;
import gr.aegean.service.AppUserDetailsService;
import gr.aegean.service.AuthService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class,
        AuthConfig.class,
        AppUserDetailsService.class,
        DeserializerConfig.class,
        JwtConfig.class,
        CustomAccessDeniedHandler.class})
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    @MockBean
    private UserRepository userRepository;
    private static final String AUTH_PATH = "/api/v1/auth";

    @Test
    void shouldReturnJwtTokenAndHTTP201WhenUserIsRegisteredSuccessfully() throws Exception {
        //Arrange
        String requestBody = """
                {
                  "firstname" : "Test",
                  "lastname" : "Test",
                  "email" : "test@example.com",
                  "password" : "CyN549^*o2Cr",
                  "role": "Employee"
                }
                """;
        String responseBody = """
                {
                    "token": "jwtToken"
                }
                """;

        AuthResponse authResponse = new AuthResponse("jwtToken");
        when(authService.registerUser(any(RegisterRequest.class))).thenReturn(authResponse);

        //Act Assert
        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isCreated(),
                        content().json(responseBody)
                );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterFirstnameIsNullOrEmpty(String firstname) throws Exception {
        String firstnameValue = firstname == null ? "null" : "\"" + firstname + "\"";
        String requestBody = String.format("""
                {
                    "firstname": %s,
                    "lastname" : "Test",
                    "email" : "test@example.com",
                    "password" : "CyN549^*o2Cr",
                    "role": "Employee"
                }
                """, firstnameValue);
        String responseBody = """
                {
                    "message": "The First Name field is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterLastnameIsNullOrEmpty(String lastname) throws Exception {
        String lastnameValue = lastname == null ? "null" : "\"" + lastname + "\"";
        String requestBody = String.format("""
                {
                    "firstname": "Test",
                    "lastname": %s,
                    "email" : "test@example.com",
                    "password" : "CyN549^*o2Cr",
                    "role": "Employee"
                }
                """, lastnameValue);
        String responseBody = """
                {
                    "message": "The Last Name field is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterEmailIsNullOrEmpty(String email) throws Exception {
        String emailValue = email == null ? "null" : "\"" + email + "\"";
        String requestBody = String.format("""
                {
                    "firstname": "Test",
                    "lastname": "Test",
                    "email" : %s,
                    "password" : "CyN549^*o2Cr",
                    "role": "Employee"
                }
                """, emailValue);
        String responseBody = """
                {
                    "message": "The Email field is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenRegisterPasswordIsNullOrEmpty(String password) throws Exception {
        String passwordValue = password == null ? "null" : "\"" + password + "\"";
        String requestBody = String.format("""
                {
                    "firstname": "Test",
                    "lastname": "Test",
                    "email": "test@example.com",
                    "password": %s,
                     "role": "Employee"
                }
                """, passwordValue);
        String responseBody = """
                {
                    "message": "The Password field is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(authService);
    }

    /*
        Empty case is handled by the deserializer
     */
    @Test
    void shouldReturnHTTP400WhenRegisterRoleIsNull() throws Exception {
        String requestBody = """
                {
                  "firstname" : "Test",
                  "lastname" : "Test",
                  "email" : "test@example.com",
                  "password" : "CyN549^*o2Cr",
                  "role": null
                }
                """;
        String responseBody = """
                {
                    "message": "The Role field is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(authService);
    }

    @Test
    void shouldReturnJwtTokenAndHTTP200WhenUserIsLoggedInSuccessfully() throws Exception {
        String requestBody = """
                {
                    "email": "test@example.com",
                    "password": "Igw4UQAlfX$E"
                }
                """;
        String responseBody = """
                {
                    "token": "jwtToken"
                }
                """;

        AuthResponse authResponse = new AuthResponse("jwtToken");
        when(authService.loginUser(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post(AUTH_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().json(responseBody)
                );
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenLoginEmailIsNullOrEmpty(String email) throws Exception {
        String emailValue = email == null ? "null" : "\"" + email + "\"";
        String requestBody = String.format("""
                {
                    "email": %s,
                    "password": "Igw4UQAlfX$E"
                }
                """, emailValue);
        String responseBody = """
                {
                    "message": "The Email field is necessary",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(authService);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnHTTP400WhenLoginPasswordIsNullOrEmpty(String password) throws Exception {
        String passwordValue = password == null ? "null" : "\"" + password + "\"";
        String requestBody = String.format("""
                {
                    "email": "test@example.com",
                    "password": %s
                }
                """, passwordValue);
        String responseBody = """
                {
                    "message": "The Password field is necessary",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(AUTH_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(authService);
    }
}
