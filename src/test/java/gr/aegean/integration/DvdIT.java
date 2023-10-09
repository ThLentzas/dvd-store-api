package gr.aegean.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import gr.aegean.model.dto.dvd.DvdDTO;
import gr.aegean.model.dto.auth.AuthResponse;
import gr.aegean.AbstractIntegrationTest;
import gr.aegean.model.dvd.DvdGenre;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.UUID;


class DvdIT extends AbstractIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    private final String AUTH_PATH = "/api/v1/auth";
    private final String DVDS_PATH = "/api/v1/dvds";

    @Test
    void shouldCreateDvd() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@gmail.com",
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

        String jwtToken = response.token();

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Fellowship of the Ring",
                     "genre": "Adventure",
                     "quantity": 3
                 }
                """;

        EntityExchangeResult<DvdDTO> result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        String locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Fellowship of the Ring");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(3);

        webTestClient.get()
                .uri(DVDS_PATH + "/{dvdID}", dvdId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(dvdId)
                .jsonPath("$.title").isEqualTo("Lord of the Rings: The Fellowship of the Ring")
                .jsonPath("$.genre").isEqualTo("ADVENTURE")
                .jsonPath("$.quantity").isEqualTo(3);
    }

    @Test
    void shouldUpdateDvd() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@gmail.com",
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

        String jwtToken = response.token();

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Fellowship of the Ring",
                     "genre": "Adventure",
                     "quantity": 4
                 }
                """;

        EntityExchangeResult<DvdDTO> result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        String locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Fellowship of the Ring");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(4);

        requestBody = """
                 {
                     "quantity": 8
                 }
                """;

        webTestClient.put()
                .uri(DVDS_PATH + "/{dvdID}", dvdId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(dvdId)
                .jsonPath("$.title").isEqualTo("Lord of the Rings: The Fellowship of the Ring")
                .jsonPath("$.genre").isEqualTo("ADVENTURE")
                .jsonPath("$.quantity").isEqualTo(8);
    }

    @Test
    void shouldFindDvdByID() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@gmail.com",
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

        String jwtToken = response.token();

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Fellowship of the Ring",
                     "genre": "Adventure",
                     "quantity": 4
                 }
                """;

        EntityExchangeResult<DvdDTO> result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        String locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Fellowship of the Ring");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(4);

        webTestClient.get()
                .uri(DVDS_PATH + "/{dvdID}", dvdId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(dvdId)
                .jsonPath("$.title").isEqualTo("Lord of the Rings: The Fellowship of the Ring")
                .jsonPath("$.genre").isEqualTo("ADVENTURE")
                .jsonPath("$.quantity").isEqualTo(4);

    }

    @Test
    void shouldFindDvdsByTitle() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@gmail.com",
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

        String jwtToken = response.token();

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Fellowship of the Ring",
                     "genre": "Adventure",
                     "quantity": 4
                 }
                """;

        EntityExchangeResult<DvdDTO> result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        String locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Fellowship of the Ring");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(4);

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Two Towers",
                     "genre": "Adventure",
                     "quantity": 3
                 }
                """;

        result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId1 = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId1));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Two Towers");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(3);

        String title = "Lord";
        webTestClient.get()
                .uri(DVDS_PATH + "?title={title}", title)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(dvdId)
                .jsonPath("$[0].title").isEqualTo("Lord of the Rings: The Fellowship of the Ring")
                .jsonPath("$[0].genre").isEqualTo("ADVENTURE")
                .jsonPath("$[0].quantity").isEqualTo(4)
                .jsonPath("$[1].id").isEqualTo(dvdId1)
                .jsonPath("$[1].title").isEqualTo("Lord of the Rings: The Two Towers")
                .jsonPath("$[1].genre").isEqualTo("ADVENTURE")
                .jsonPath("$[1].quantity").isEqualTo(3);
    }

    @Test
    void shouldFindAllDvds() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@gmail.com",
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

        String jwtToken = response.token();

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Fellowship of the Ring",
                     "genre": "Adventure",
                     "quantity": 4
                 }
                """;

        EntityExchangeResult<DvdDTO> result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        String locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Fellowship of the Ring");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(4);

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Two Towers",
                     "genre": "Adventure",
                     "quantity": 3
                 }
                """;

        result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId1 = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId1));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Two Towers");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(3);

        webTestClient.get()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.size()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo(dvdId)
                .jsonPath("$[0].title").isEqualTo("Lord of the Rings: The Fellowship of the Ring")
                .jsonPath("$[0].genre").isEqualTo("ADVENTURE")
                .jsonPath("$[0].quantity").isEqualTo(4)
                .jsonPath("$[1].id").isEqualTo(dvdId1)
                .jsonPath("$[1].title").isEqualTo("Lord of the Rings: The Two Towers")
                .jsonPath("$[1].genre").isEqualTo("ADVENTURE")
                .jsonPath("$[1].quantity").isEqualTo(3);
    }

    @Test
    void shouldDeleteDvdByID() {
        String requestBody = """
                {
                    "firstname": "Employee",
                    "lastname": "Employee",
                    "email": "employee@gmail.com",
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

        String jwtToken = response.token();

        requestBody = """
                 {
                     "title": "Lord of the Rings: The Fellowship of the Ring",
                     "genre": "Adventure",
                     "quantity": 4
                 }
                """;

        EntityExchangeResult<DvdDTO> result = webTestClient.post()
                .uri(DVDS_PATH)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(DvdDTO.class)
                .returnResult();

        //We are extracting the id of the newly created resource to use it for subsequent requests
        String locationHeader = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        String dvdId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

        assertThat(result.getResponseBody().id()).isEqualTo(UUID.fromString(dvdId));
        assertThat(result.getResponseBody().title()).isEqualTo("Lord of the Rings: The Fellowship of the Ring");
        assertThat(result.getResponseBody().genre()).isEqualTo(DvdGenre.ADVENTURE);
        assertThat(result.getResponseBody().quantity()).isEqualTo(4);

        webTestClient.delete()
                .uri(DVDS_PATH + "/{dvdId}", dvdId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.get()
                .uri(DVDS_PATH + "/{dvdId}", dvdId)
                .accept(MediaType.APPLICATION_JSON)
                .header(AUTHORIZATION, String.format("Bearer %s", jwtToken))
                .exchange()
                .expectStatus().isNotFound();
    }
}
