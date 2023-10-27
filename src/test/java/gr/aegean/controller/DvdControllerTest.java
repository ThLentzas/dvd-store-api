package gr.aegean.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gr.aegean.config.DeserializerConfig;
import gr.aegean.config.security.AuthConfig;
import gr.aegean.config.security.JwtConfig;
import gr.aegean.config.security.SecurityConfig;
import gr.aegean.exception.CustomAccessDeniedHandler;
import gr.aegean.exception.ResourceNotFoundException;
import gr.aegean.repository.UserRepository;
import gr.aegean.service.DvdService;
import gr.aegean.model.dto.dvd.DvdCreateRequest;
import gr.aegean.model.dto.dvd.DvdDTO;
import gr.aegean.model.dvd.DvdGenre;
import gr.aegean.model.dto.dvd.DvdUpdateRequest;
import gr.aegean.service.AppUserDetailsService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


@WebMvcTest(DvdController.class)
@Import({SecurityConfig.class,
        AuthConfig.class,
        AppUserDetailsService.class,
        DeserializerConfig.class,
        JwtConfig.class,
        CustomAccessDeniedHandler.class})
class DvdControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DvdService dvdService;
    @MockBean
    private UserRepository userRepository;
    private static final String DVD_PATH = "/api/v1/dvds";

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnDvdAndHTTP201WhenUserIsAuthorized() throws Exception {
        String requestBody = """
                {
                    "title": "Lord of the Rings: The Fellowship of the Ring",
                    "genre": "Adventure",
                    "quantity": 5
                }
                """;

        DvdDTO dvdDTO = generateDvd(UUID.randomUUID());
        String responseBody = String.format("""
                {   "id": "%s",
                    "title": "Lord of the Rings: The Fellowship of the Ring",
                    "genre": "ADVENTURE",
                    "quantity": 5
                }
                """, dvdDTO.id().toString());

        when(dvdService.createDvd(any(DvdCreateRequest.class))).thenReturn(dvdDTO);

        mockMvc.perform(post(DVD_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isCreated(),
                        header().string("Location", containsString(DVD_PATH + "/" + dvdDTO.id())),
                        content().json(responseBody)
                );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP400WhenTitleIsNullOrEmpty(String title) throws Exception {
        String titleValue = title == null ? "null" : "\"" + title + "\"";
        String requestBody = String.format("""
                {
                    "title": %s,
                    "genre": "ADVENTURE",
                    "quantity": 5
                }
                """, titleValue);
        String responseBody = """
                {
                    "message": "The title is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(DVD_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(dvdService);
    }

    /*
        Any other case is cover by the deserializer
     */
    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP400WhenGenreIsNull() throws Exception {
        String requestBody = """
                {
                    "title": "Lord of the Rings: The Fellowship of the Ring",
                    "genre": null,
                    "quantity": 5
                }
                """;
        String responseBody = """
                {
                    "message": "The genre is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(DVD_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(dvdService);
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP400WhenQuantityIsNull() throws Exception {
        String requestBody = """
                {
                    "title": "Lord of the Rings: The Fellowship of the Ring",
                    "genre": "ADVENTURE",
                    "quantity": null
                }
                """;
        String responseBody = """
                {
                    "message": "The quantity is required",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(DVD_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(dvdService);
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP400WhenTitleExceedsMaxLength() throws Exception {
        String titleValue = RandomStringUtils.randomAlphabetic(101);
        String requestBody = String.format("""
                {
                    "title": "%s",
                    "genre": "ADVENTURE",
                    "quantity": 5
                }
                """, titleValue);
        String responseBody = """
                {
                    "message": "Invalid title. Title must not exceed 100 characters",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(DVD_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(dvdService);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP400WhenQuantityIsNegativeOrZeroForDvdCreateRequest(Integer quantity) throws Exception {
        String requestBody = String.format("""
                {
                    "title": "Lord of the Rings: The Fellowship of the Ring",
                    "genre": "ADVENTURE",
                    "quantity": %d
                }
                """, quantity);
        String responseBody = """
                {
                    "message": "The quantity must be a positive number",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(post(DVD_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );

        verifyNoInteractions(dvdService);
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnHTTP403WhenUserIsNotAuthorizedToCreateDvd() throws Exception {
        String requestBody = """
                {
                    "title": "Lord of the Rings: The Fellowship of the Ring",
                    "genre": "ADVENTURE",
                    "quantity": 5
                }
                """;

        mockMvc.perform(put(DVD_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dvdService);
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP200AndUpdatedDvdWhenUserIsAuthorized() throws Exception {
        String requestBody = """
                {
                    "quantity": 8,
                    "genre": "SCIENCE_FICTION"
                }
                """;
        String responseBody = """
                {
                    "title": "Interstellar",
                    "genre": "SCIENCE_FICTION",
                    "quantity": 8
                }
                """;

        DvdDTO dvdDTO = new DvdDTO(UUID.randomUUID(), "Interstellar", DvdGenre.SCIENCE_FICTION, 8);

        when(dvdService.updateDvd(any(String.class), any(DvdUpdateRequest.class))).thenReturn(dvdDTO);

        mockMvc.perform(put(DVD_PATH + '/' + "{dvdId}", dvdDTO.id().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().json(responseBody)
                );
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP400WhenQuantityIsNegativeOrZeroForDvdUpdateRequest(Integer quantity) throws Exception {
        String requestBody = String.format("""
                {
                    "quantity": %d,
                    "genre": "SCIENCE_FICTION"
                }
                """, quantity);
        String responseBody = """
                {
                    "message": "The quantity must be a positive number",
                    "statusCode": 400
                }
                """;

        mockMvc.perform(put(DVD_PATH + '/' + "{dvdId}", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP404WhenDvdIsNotFoundToUpdate() throws Exception {
        String requestBody = """
                {
                    "quantity": 5
                }
                """;

        String id = UUID.randomUUID().toString();
        String responseBody = String.format("""
                {
                    "message": "Dvd was not found with id: %s",
                    "statusCode": 404
                }
                """, id);

        when(dvdService.updateDvd(any(String.class), any(DvdUpdateRequest.class))).thenThrow(
                new ResourceNotFoundException("Dvd was not found with id: " + id));

        mockMvc.perform(put(DVD_PATH + '/' + "{dvdId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnHTTP403WhenUserIsNotAuthorizedToUpdateDvd() throws Exception {
        String requestBody = """
                {
                    "quantity": 5
                }
                """;

        mockMvc.perform(put(DVD_PATH + '/' + "{dvdId}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dvdService);
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnDvdAndHTTP200WhenUserIsAuthorized() throws Exception {
        DvdDTO dvdDTO = generateDvd(UUID.randomUUID());
        String responseBody = String.format("""
                {   "id": "%s",
                    "title": "Lord of the Rings: The Fellowship of the Ring",
                    "genre": "ADVENTURE",
                    "quantity": 5
                }
                """, dvdDTO.id());

        when(dvdService.findDvdById(dvdDTO.id().toString())).thenReturn(dvdDTO);

        mockMvc.perform(get(DVD_PATH + '/' + "{dvdId}", dvdDTO.id())
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnHTTP404WhenDvdIsNotFound() throws Exception {
        String id = UUID.randomUUID().toString();
        String responseBody = String.format("""
                {
                    "message": "Dvd was not found with id: %s",
                    "statusCode": 404
                }
                """, id);

        when(dvdService.findDvdById(any(String.class))).thenThrow(
                new ResourceNotFoundException("Dvd was not found with id: " + id));

        mockMvc.perform(get(DVD_PATH + '/' + "{dvdId}", UUID.randomUUID())
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isNotFound(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnHTTP403WhenUserIsNotAuthorizedToFindDvdById() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get(DVD_PATH + '/' + "{dvdId}", id.toString())
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dvdService);
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnListOfDvdsMatchingTitleAndHTTP200WhenUserIsAuthorized() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<DvdDTO> dvdsDTO = generateDvds(id1, id2);

        String title = "Lord";

        String responseBody = String.format("""
                [
                    {
                        "id": "%s",
                        "title": "Lord of the Rings: The Fellowship of the Ring",
                        "genre": "ADVENTURE",
                        "quantity": 5
                    }, {
                        "id": "%s",
                        "title": "Lord of the Rings: The Two Towers",
                        "genre": "ADVENTURE",
                        "quantity": 5
                    }
                ]
                """, id1, id2);

        when(dvdService.findDvds(title)).thenReturn(dvdsDTO);

        mockMvc.perform(get(DVD_PATH + "?title={title}", title)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnAnEmptyListWhenNoDvdsMatchedTheProvidedTitleAndHTTP200ForAuthorizedUser() throws Exception {
        String title = "Lord";

        String responseBody = """
                    []
                """;

        when(dvdService.findDvds(title)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(DVD_PATH + "?title={title}", title)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnListOfAllDvdsWhenNoTitleWasProvidedAndHTTP200ForAuthorizedUser() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<DvdDTO> dvdsDTO = generateDvds(id1, id2);

        String responseBody = String.format("""
                [
                    {
                        "id": "%s",
                        "title": "Lord of the Rings: The Fellowship of the Ring",
                        "genre": "ADVENTURE",
                        "quantity": 5
                    }, {
                        "id": "%s",
                        "title": "Lord of the Rings: The Two Towers",
                        "genre": "ADVENTURE",
                        "quantity": 5
                    }
                ]
                """, id1, id2);

        when(dvdService.findDvds(null)).thenReturn(dvdsDTO);

        mockMvc.perform(get(DVD_PATH)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnAnEmptyListWhenNoDvdsFoundAndHTTP200ForAuthorizedUser() throws Exception {
        String responseBody = """
                    []
                """;

        when(dvdService.findDvds(null)).thenReturn(Collections.emptyList());

        mockMvc.perform(get(DVD_PATH)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isOk(),
                        content().json(responseBody)
                );
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnHTTP403WhenUserIsNotAuthorizedToFindDvds() throws Exception {
        mockMvc.perform(get(DVD_PATH)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dvdService);
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldDeleteDvdWhenUserIsAuthorized() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(DVD_PATH + '/' + id))
                .andExpect(status().isNoContent());

        verify(dvdService, times(1)).deleteDvd(id.toString());
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnHTTP403WhenUserIsNotAuthorizedToDeleteDvd() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(DVD_PATH + '/' + id))
                .andExpect(status().isForbidden());

        verifyNoInteractions(dvdService);
    }

    private DvdDTO generateDvd(UUID id) {
        return new DvdDTO(
                id,
                "Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5);
    }

    private List<DvdDTO> generateDvds(UUID id1, UUID id2) {
        DvdDTO dvdDTO1 = new DvdDTO(
                id1,
                "Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5);

        DvdDTO dvdDTO2 = new DvdDTO(
                id2,
                "Lord of the Rings: The Two Towers",
                DvdGenre.ADVENTURE,
                5);

        return List.of(dvdDTO1, dvdDTO2);
    }
}
