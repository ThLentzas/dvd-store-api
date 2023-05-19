package gr.aegean.integration;

import gr.aegean.security.auth.AuthResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles(profiles = "test")
public class AuthIT extends AbstractIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    private final String AUTH_PATH = "/api/v1/auth";

    @Test
    void shouldLoginUser() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@gmail.com",
                    "password": "CyN549^*o2Cr",
                    "role": "Employee"
                }""";

        String jwtToken = webTestClient.post()
                .uri(AUTH_PATH + "/signup")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();

        assertThat(jwtToken).isNotNull();

        requestBody = """
                {
                    "email": "employee@gmail.com",
                    "password": "CyN549^*o2Cr"
                }""";

        jwtToken = webTestClient.post()
                .uri(AUTH_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();

        assertThat(jwtToken).isNotNull();
    }
}
