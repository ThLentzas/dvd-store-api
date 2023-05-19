package gr.aegean.integration;

import gr.aegean.model.dvd.DvdDTO;
import gr.aegean.security.auth.AuthResponse;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "10000")
@ActiveProfiles(profiles = "test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DvdIT extends AbstractIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    private final String AUTH_PATH = "/api/v1/auth";
    private final String DVDS_PATH = "/api/v1/dvds";

    @Test
    @Order(1)
    void shouldCreateDvd() {
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
                .expectStatus().isCreated()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();

        requestBody = """
                 {
                     "title": "Lord, of the Rings: The Fellowship of the Ring",
                     "genre": "Adventure",
                     "quantity": 3
                 }
                """;

        DvdDTO createdDvdDTO = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdDvdDTO).isNotNull();
    }

    @Test
    @Order(2)
    void shouldUpdateDvd() {
        String requestBody = """
                {
                    "email": "employee@gmail.com",
                    "password": "CyN549^*o2Cr"
                }""";

        String jwtToken = webTestClient.post()
                .uri(AUTH_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();

        requestBody = """
                 {
                     "title": "Lord, of the Rings: The Two Towers",
                     "genre": "Adventure",
                     "quantity": 4
                 }
                """;

        DvdDTO createdDvdDTO = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdDvdDTO).isNotNull();

        requestBody = """
                 {
                     "quantity": 8
                 }
                """;

        DvdDTO updatedDvdDTO = webTestClient.put()
                .uri(DVDS_PATH + "/{dvdID}", createdDvdDTO.id())
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(updatedDvdDTO.quantity()).isEqualTo(8);
    }

    @Test
    @Order(3)
    void shouldFindDvdByID() {
        String requestBody = """
                {
                    "email": "employee@gmail.com",
                    "password": "CyN549^*o2Cr"
                }""";

        String jwtToken = webTestClient.post()
                .uri(AUTH_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();


        requestBody = """
                 {
                     "title": "Interstellar",
                     "genre": "Science_fiction",
                     "quantity": 2
                 }
                """;

        DvdDTO createdDvdDTO = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(createdDvdDTO).isNotNull();

        DvdDTO retrievedDvdDTO = webTestClient.get()
                .uri(DVDS_PATH + "/{dvdID}", createdDvdDTO.id())
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(retrievedDvdDTO).isNotNull();
    }

    @Test
    @Order(4)
    void shouldFindDvdsByTitle() {
        String requestBody = """
                {
                    "email": "employee@gmail.com",
                    "password": "CyN549^*o2Cr"
                }""";

        String jwtToken = webTestClient.post()
                .uri(AUTH_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();

        String searchTitle = "Lord";
        List<DvdDTO> retrievedDvdsDTO = webTestClient.get()
                .uri(DVDS_PATH + "?title={title}", searchTitle)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(retrievedDvdsDTO).hasSize(2);
    }

    @Test
    @Order(5)
    void shouldFindAllDvds() {
        String requestBody = """
                {
                    "email": "employee@gmail.com",
                    "password": "CyN549^*o2Cr"
                }""";

        String jwtToken = webTestClient.post()
                .uri(AUTH_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();

        List<DvdDTO> retrievedDvdsDTO = webTestClient.get()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(retrievedDvdsDTO).hasSize(3);
    }

    @Test
    @Order(6)
    void shouldDeleteDvdByID() {
        String requestBody = """
                {
                    "email": "employee@gmail.com",
                    "password": "CyN549^*o2Cr"
                }""";

        String jwtToken = webTestClient.post()
                .uri(AUTH_PATH + "/login")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthResponse.class)
                .returnResult()
                .getResponseBody()
                .getToken();

        requestBody = """
                 {
                     "title": "Inception",
                     "genre": "Science_fiction",
                     "quantity": 7
                 }
                """;

        DvdDTO toBeDeletedDvdDTO = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DvdDTO.class)
                .returnResult()
                .getResponseBody();

        assertThat(toBeDeletedDvdDTO).isNotNull();

        webTestClient.delete()
                .uri(DVDS_PATH + "/{dvdID}", toBeDeletedDvdDTO.id())
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isNoContent();

       webTestClient.get()
                .uri(DVDS_PATH + "/{dvdID}", toBeDeletedDvdDTO.id())
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isNotFound();
    }
}
