package gr.aegean.controller;

import com.zaxxer.hikari.HikariDataSource;
import gr.aegean.config.*;
import gr.aegean.exception.CustomAccessDeniedHandler;
import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdDTO;
import gr.aegean.model.dvd.DvdGenre;
import gr.aegean.repository.UserRepository;
import gr.aegean.service.DvdService;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(DvdController.class)
@Import({SecurityConfig.class,
        AuthConfig.class,
        DataSourceConfig.class,
        JwtConfig.class,
        RedisProperties.class,
        UserRepository.class,
        HikariDataSource.class,
        CustomAccessDeniedHandler.class})
public class DvdControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DvdService dvdService;
    private final String DVDS_PATH = "/api/v1/dvds";

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnCreatedDvdWhenUserIsAuthorized() throws Exception {
        //Arrange
        String requestBody = """
                {
                    "title": "Interstellar",
                    "genre": "SCIENCE_FICTION",
                    "quantity": 5
                }
                """;
        UUID id = UUID.randomUUID();
        DvdDTO createdDvdDTO = new DvdDTO(id, "Interstellar", DvdGenre.SCIENCE_FICTION, 5,
                List.of(Link.of(DVDS_PATH + "/" + id, "self"),
                        Link.of(DVDS_PATH, "allDvds")));

        when(dvdService.createDvd(any(Dvd.class))).thenReturn(createdDvdDTO);

        //Act Assert
        mockMvc.perform(post(DVDS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", Matchers.containsString(DVDS_PATH + "/" +
                        createdDvdDTO.id())))
                .andExpect(jsonPath("$.id", is(createdDvdDTO.id().toString())))
                .andExpect(jsonPath("$.title", is("Interstellar")))
                .andExpect(jsonPath("$.genre", is("SCIENCE_FICTION")))
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.links[0].rel", is("self")))
                .andExpect(jsonPath("$.links[0].href", Matchers.containsString(
                        DVDS_PATH + "/" + createdDvdDTO.id())))
                .andExpect(jsonPath("$.links[1].rel", is("allDvds")))
                .andExpect(jsonPath("$.links[1].href", Matchers.containsString(DVDS_PATH)));
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedToCreateDvd() throws Exception {
        //Arrange
        String requestBody = """
                {
                    "title": "Interstellar",
                    "genre": "Science_fiction",
                    "quantity": 5
                }
                """;

        //Act Assert
        mockMvc.perform(put(DVDS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnUpdatedDvdWhenUserIsAuthorized() throws Exception {
        //Arrange
        String requestBody = """
                {
                    "quantity": 8
                }
                """;
        UUID id = UUID.randomUUID();
        DvdDTO createdDvdDTO = new DvdDTO(id, "Interstellar", DvdGenre.SCIENCE_FICTION, 8,
                List.of(Link.of(DVDS_PATH + "/" + id, "self"),
                        Link.of(DVDS_PATH, "allDvds")));

        when(dvdService.updateDvd(id.toString(), 8, null)).thenReturn(createdDvdDTO);

        //Act Assert
        mockMvc.perform(put(DVDS_PATH + "/" + "{dvdID}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdDvdDTO.id().toString())))
                .andExpect(jsonPath("$.title", is("Interstellar")))
                .andExpect(jsonPath("$.genre", is("SCIENCE_FICTION")))
                .andExpect(jsonPath("$.quantity", is(8)))
                .andExpect(jsonPath("$.links[0].rel", is("self")))
                .andExpect(jsonPath("$.links[0].href", Matchers.containsString(
                        DVDS_PATH + "/" + createdDvdDTO.id())))
                .andExpect(jsonPath("$.links[1].rel", is("allDvds")))
                .andExpect(jsonPath("$.links[1].href", Matchers.containsString(DVDS_PATH)));
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedToUpdateDvd() throws Exception {
        //Arrange
        String requestBody = """
                {
                    "quantity": 5
                }
                """;

        //Act Assert
        mockMvc.perform(put(DVDS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnDvdByIDWhenUserIsAuthorized() throws Exception {
        //Arrange
        UUID id = UUID.randomUUID();
        DvdDTO createdDvdDTO = new DvdDTO(id, "Interstellar", DvdGenre.SCIENCE_FICTION, 8,
                List.of(Link.of(DVDS_PATH + "/" + id, "self"),
                        Link.of(DVDS_PATH, "allDvds")));

        when(dvdService.findDvdByID(id.toString())).thenReturn(createdDvdDTO);

        //Act Assert
        mockMvc.perform(get(DVDS_PATH + "/" + "{dvdID}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdDvdDTO.id().toString())))
                .andExpect(jsonPath("$.title", is("Interstellar")))
                .andExpect(jsonPath("$.genre", is("SCIENCE_FICTION")))
                .andExpect(jsonPath("$.quantity", is(8)))
                .andExpect(jsonPath("$.links[0].rel", is("self")))
                .andExpect(jsonPath("$.links[0].href", Matchers.containsString(
                        DVDS_PATH + "/" + createdDvdDTO.id())))
                .andExpect(jsonPath("$.links[1].rel", is("allDvds")))
                .andExpect(jsonPath("$.links[1].href", Matchers.containsString(DVDS_PATH)));
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedToFindDvdByID() throws Exception {
        //Arrange
        UUID id = UUID.randomUUID();

        //Act Assert
        mockMvc.perform(get(DVDS_PATH + "/" + "{dvdID}", id.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnListOfDvdsByTitleWhenUserIsAuthorized() throws Exception {
        //Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        String searchTitle = "Lord";

        DvdDTO dvdDTO1 = new DvdDTO(id1, "Lord, of the Rings: The Fellowship of the Ring", DvdGenre.ADVENTURE, 5,
                List.of(Link.of(DVDS_PATH + "/" + id1, "self"),
                        Link.of(DVDS_PATH, "allDvds")));
        DvdDTO dvdDTO2 = new DvdDTO(id2, "Lord, of the Rings: The Two Towers", DvdGenre.ADVENTURE, 7,
                List.of(Link.of(DVDS_PATH + "/" + id2, "self"),
                        Link.of(DVDS_PATH, "allDvds")));

        when(dvdService.findDvds(searchTitle)).thenReturn(List.of(dvdDTO1, dvdDTO2));

        //Act Assert
        mockMvc.perform(get(DVDS_PATH + "?title={title}", searchTitle))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id", is(id1.toString())))
                .andExpect(jsonPath("$[0].title", is("Lord, of the Rings: The Fellowship " +
                        "of the Ring")))
                .andExpect(jsonPath("$[0].genre", is("ADVENTURE")))
                .andExpect(jsonPath("$[0].quantity", is(5)))
                .andExpect(jsonPath("$[0].links[0].rel", is("self")))
                .andExpect(jsonPath("$[0].links[0].href", Matchers.containsString(
                        DVDS_PATH + "/" + id1)))
                .andExpect(jsonPath("$[0].links[1].rel", is("allDvds")))
                .andExpect(jsonPath("$[0].links[1].href", Matchers.containsString(DVDS_PATH)))
                .andExpect(jsonPath("$[1].id", is(id2.toString())))
                .andExpect(jsonPath("$[1].title", is("Lord, of the Rings: The Two Towers")))
                .andExpect(jsonPath("$[1].genre", is("ADVENTURE")))
                .andExpect(jsonPath("$[1].quantity", is(7)))
                .andExpect(jsonPath("$[1].links[0].rel", is("self")))
                .andExpect(jsonPath("$[1].links[0].href", Matchers.containsString(
                        DVDS_PATH + "/" + id2)))
                .andExpect(jsonPath("$[1].links[1].rel", is("allDvds")))
                .andExpect(jsonPath("$[1].links[1].href", Matchers.containsString(DVDS_PATH)));
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedToFindDvdByTitle() throws Exception {
        //Arrange
        String searchTitle = "Lord";

        //Act Assert
        mockMvc.perform(get(DVDS_PATH + "?title={title}", searchTitle))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldReturnListOfAllDvdsWhenUserIsAuthorized() throws Exception {
        //Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        DvdDTO dvdDTO1 = new DvdDTO(id1, "Interstellar", DvdGenre.SCIENCE_FICTION, 5,
                List.of(Link.of(DVDS_PATH + "/" + id1, "self"),
                        Link.of(DVDS_PATH, "allDvds")));
        DvdDTO dvdDTO2 = new DvdDTO(id2, "Inception", DvdGenre.SCIENCE_FICTION, 8,
                List.of(Link.of(DVDS_PATH + "/" + id2, "self"),
                        Link.of(DVDS_PATH, "allDvds")));

        when(dvdService.findDvds(null)).thenReturn(List.of(dvdDTO1, dvdDTO2));

        //Act Assert
        mockMvc.perform(get(DVDS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(2)))
                .andExpect(jsonPath("$[0].id", is(id1.toString())))
                .andExpect(jsonPath("$[0].title", is("Interstellar")))
                .andExpect(jsonPath("$[0].genre", is("SCIENCE_FICTION")))
                .andExpect(jsonPath("$[0].quantity", is(5)))
                .andExpect(jsonPath("$[0].links[0].rel", is("self")))
                .andExpect(jsonPath("$[0].links[0].href", Matchers.containsString(
                        DVDS_PATH + "/" + id1)))
                .andExpect(jsonPath("$[0].links[1].rel", is("allDvds")))
                .andExpect(jsonPath("$[0].links[1].href", Matchers.containsString(DVDS_PATH)))
                .andExpect(jsonPath("$[1].id", is(id2.toString())))
                .andExpect(jsonPath("$[1].title", is("Inception")))
                .andExpect(jsonPath("$[1].genre", is("SCIENCE_FICTION")))
                .andExpect(jsonPath("$[1].quantity", is(8)))
                .andExpect(jsonPath("$[1].links[0].rel", is("self")))
                .andExpect(jsonPath("$[1].links[0].href", Matchers.containsString(
                        DVDS_PATH + "/" + id2)))
                .andExpect(jsonPath("$[1].links[1].rel", is("allDvds")))
                .andExpect(jsonPath("$[1].links[1].href", Matchers.containsString(DVDS_PATH)));
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedToFindAllDvds() throws Exception {
        //Act Assert
        mockMvc.perform(get(DVDS_PATH))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test", roles = "EMPLOYEE")
    void shouldDeleteDvdWhenUserIsAuthorized() throws Exception {
        //Arrange
        UUID id = UUID.randomUUID();

        //Act Assert
        mockMvc.perform(delete(DVDS_PATH + "/" + id))
                .andExpect(status().isNoContent());

        verify(dvdService, times(1)).deleteDvd(id.toString());
    }

    @Test
    @WithMockUser(username = "test", roles = "CUSTOMER")
    void shouldReturnForbiddenWhenUserIsNotAuthorizedToDeleteDvd() throws Exception {
        //Arrange
        UUID id = UUID.randomUUID();

        //Act Assert
        mockMvc.perform(delete(DVDS_PATH + "/" + id))
                .andExpect(status().isForbidden());
    }
}
