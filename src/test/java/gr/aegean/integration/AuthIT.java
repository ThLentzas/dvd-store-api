package gr.aegean.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import gr.aegean.model.dto.auth.AuthResponse;
import gr.aegean.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;


@AutoConfigureWebTestClient
class AuthIT extends AbstractIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    private final String AUTH_PATH = "/api/v1/auth";

    @Test
    void shouldLoginUser() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@example.com",
                    "password": "CyN549^*o2Cr",
                    "role": "Employee"
                }""";

        AuthResponse response = webTestClient.post()
                .uri(AUTH_PATH + "/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.token()).isNotNull();

        requestBody = """
                {
                    "email": "employee@example.com",
                    "password": "CyN549^*o2Cr"
                }""";

        response = webTestClient.post()
                .uri(AUTH_PATH + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response.token()).isNotNull();
    }
}
