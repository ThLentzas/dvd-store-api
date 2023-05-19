package gr.aegean.controller;

import com.zaxxer.hikari.HikariDataSource;

import gr.aegean.config.AuthConfig;
import gr.aegean.config.DataSourceConfig;
import gr.aegean.config.JwtConfig;
import gr.aegean.config.RedisProperties;
import gr.aegean.config.SecurityConfig;
import gr.aegean.exception.CustomAccessDeniedHandler;
import gr.aegean.repository.UserRepository;
import gr.aegean.security.auth.AuthRequest;
import gr.aegean.security.auth.AuthResponse;
import gr.aegean.security.auth.RegisterRequest;
import gr.aegean.service.AuthService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class,
        AuthConfig.class,
        DataSourceConfig.class,
        JwtConfig.class,
        RedisProperties.class,
        UserRepository.class,
        HikariDataSource.class,
        CustomAccessDeniedHandler.class})
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AuthService authService;
    private final String AUTH_PATH = "/api/v1/auth";

    @Test
    void shouldReturnJwtTokenWhenUserIsRegisteredSuccessfully() throws Exception {
        //Arrange
        String requestBody = """
                {
                  "firstname" : "Employee",
                  "lastname" : "Employee",
                  "email" : "employee@gmail.com",
                  "password" : "CyN549^*o2Cr",
                  "role": "ROLE_EMPLOYEE"
                }
                """;
        AuthResponse authResponse = new AuthResponse("jwtToken", 1);

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        //Act Assert
        mockMvc.perform(post(AUTH_PATH + "/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString("api/v1/users/" + 1)))
                .andExpect(jsonPath("$.token", is("jwtToken")));
    }

    @Test
    void shouldReturnJwtTokenWhenUserIsAuthenticatedSuccessfully() throws Exception {
        //Arrange
        String requestBody = """
                {
                  "email" : "employee@gmail.com",
                  "password" : "CyN549^*o2Cr"
                }
                """;
        AuthResponse authResponse = new AuthResponse("jwtToken");

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(authResponse);

        //Act Assert
        mockMvc.perform(post(AUTH_PATH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("jwtToken")));
    }
}
