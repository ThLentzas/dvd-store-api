package gr.aegean.service;

import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.exception.ResourceNotFoundException;
import gr.aegean.AbstractUnitTest;
import gr.aegean.repository.DvdRepository;
import gr.aegean.entity.Dvd;
import gr.aegean.model.dto.dvd.DvdCreateRequest;
import gr.aegean.model.dto.dvd.DvdDTO;
import gr.aegean.model.dvd.DvdGenre;
import gr.aegean.model.dto.dvd.DvdUpdateRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.UUID;


@ExtendWith(MockitoExtension.class)
class DvdServiceTest extends AbstractUnitTest {
    private DvdRepository dvdRepository;
    private final String CACHE_NAME = "dvds";
    private DvdService underTest;

    @BeforeEach()
    void setup() {
        dvdRepository = new DvdRepository(getJdbcTemplate());
        underTest = new DvdService(dvdRepository, redisTemplate());

        redisTemplate().delete(CACHE_NAME);
        dvdRepository.deleteAllDvds();
    }

    @Test
    void shouldCreateDvdAndStoreItInCache() {
        //Arrange
        DvdCreateRequest createRequest = generateDvd();

        //Act
        DvdDTO actual = underTest.createDvd(createRequest);

        //Assert
        assertThat(actual.title()).isEqualTo(createRequest.title());
        assertThat(actual.genre()).isEqualTo(createRequest.genre());
        assertThat(actual.quantity()).isEqualTo(createRequest.quantity());
        assertThat(redisTemplate().opsForHash()
                .hasKey(CACHE_NAME, actual.id().toString()))
                .isTrue();
    }

    @Test
    void shouldThrowDuplicateResourceExceptionIfDvdAlreadyExists() {
        // Arrange
        DvdCreateRequest createRequest = generateDvd();
        DvdCreateRequest duplicateDvdRequest = generateDvd();

        //Act
        underTest.createDvd(createRequest);

        // Assert
        assertThatThrownBy(() -> underTest.createDvd(duplicateDvdRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Dvd already exists");
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"!@#*(){}[]_^~`;?/<>,\\|\""})
    void shouldThrowIllegalArgumentExceptionWhenTitleIsInvalid(String title) {
        //Arrange
        DvdCreateRequest createRequest = new DvdCreateRequest(title, DvdGenre.ADVENTURE, 5);

        //Act Assert
        assertThatThrownBy(() -> underTest.createDvd(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No title was provided");
    }

    @Test
    void shouldUpdateDvdAndCache() {
        // Arrange
        Dvd dvd = new Dvd(
                UUID.randomUUID(),
                "Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5
        );
        dvd = dvdRepository.createDvd(dvd);

        DvdUpdateRequest updateRequest = new DvdUpdateRequest(7, DvdGenre.THRILLER);

        //Act
        DvdDTO actual = underTest.updateDvd(dvd.getId().toString(), updateRequest);
        dvd.setQuantity(7);
        dvd.setGenre(DvdGenre.THRILLER);

        //Assert
        DvdDTO expected = underTest.findDvdById(dvd.getId().toString());

        assertThat(actual).isEqualTo(expected);
        assertThat(redisTemplate().opsForHash().get(CACHE_NAME, dvd.getId().toString())).isEqualTo(dvd);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenUpdatingNeitherQuantityNorGenre() {
        //Arrange
        String dvdID = UUID.randomUUID().toString();
        DvdUpdateRequest updateRequest = new DvdUpdateRequest(null, null);

        //Act Assert
        assertThatThrownBy(() -> underTest.updateDvd(dvdID, updateRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Either quantity or genre must be provided");
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDvdIsNotFoundToUpdate() {
        //Arrange
        String id = UUID.randomUUID().toString();
        DvdUpdateRequest updateRequest = new DvdUpdateRequest(7, DvdGenre.THRILLER);

        //Act Assert
        assertThatThrownBy(() -> underTest.updateDvd(id, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dvd was not found with id: " + id);
    }

    @Test
    void shouldFindDvdByIDFromCache() {
        //Arrange
        Dvd expected = new Dvd(
                UUID.randomUUID(),
                "Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5
        );

        redisTemplate().opsForHash().put(CACHE_NAME, expected.getId().toString(), expected);

        //Act
        DvdDTO actual = underTest.findDvdById(expected.getId().toString());

        //Assert
        assertThat(actual.id()).isEqualTo(expected.getId());
        assertThat(actual.title()).isEqualTo(expected.getTitle());
        assertThat(actual.genre()).isEqualTo(expected.getGenre());
        assertThat(actual.quantity()).isEqualTo(expected.getQuantity());
    }

    @Test
    void shouldFindDvdByIDFromRepositoryAndUpdateCache() {
        //Arrange
        DvdCreateRequest createRequest = generateDvd();
        DvdDTO expected = underTest.createDvd(createRequest);
        //Creating a dvd adds it to the cache, so we remove it. In a real scenario we assume that the TTL expired.
        redisTemplate().delete(CACHE_NAME);

        //Act
        DvdDTO actual = underTest.findDvdById(expected.id().toString());

        //Assert
        assertThat(actual).isEqualTo(expected);
        assertThat(redisTemplate().opsForHash()
                .hasKey(CACHE_NAME, actual.id().toString()))
                .isTrue();
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDvdIsNotFoundById() {
        //Arrange
        String id = UUID.randomUUID().toString();

        //Act and Assert
        assertThatThrownBy(() -> underTest.findDvdById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dvd was not found with id: " + id);
    }

    @Test
    void shouldFindDvdsByTitle() {
        //Arrange
        String searchTitle = "Lord";

        DvdCreateRequest createRequest = generateDvds().get(0);
        DvdDTO dvdDTO1 = underTest.createDvd(createRequest);

        createRequest = generateDvds().get(1);
        DvdDTO dvdDTO2 = underTest.createDvd(createRequest);

        //Act
        List<DvdDTO> actual = underTest.findDvds(searchTitle);

        //Assert
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder(dvdDTO1, dvdDTO2);
    }

    @Test
    void shouldReturnAnEmptyListWhenNoDvdsMatchTheSearchTitle() {
        //Arrange
        String searchTitle = "Interstellar";
        DvdCreateRequest createRequest = generateDvd();
        //Create a Dvd with a different title
        underTest.createDvd(createRequest);

        //Act
        List<DvdDTO> actual = underTest.findDvds(searchTitle);

        //Assert
        assertThat(actual).isEmpty();
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldFindAllDvdsWhenNoTitleWasProvided(String searchTitle) {
        //Arrange
        DvdCreateRequest createRequest = generateDvds().get(0);
        DvdDTO dvdDTO1 = underTest.createDvd(createRequest);

        createRequest = generateDvds().get(1);
        DvdDTO dvdDTO2 = underTest.createDvd(createRequest);

        //Act
        List<DvdDTO> actual = underTest.findDvds(searchTitle);

        //Assert
        assertThat(actual)
                .isNotNull()
                .hasSize(2)
                .containsExactlyInAnyOrder(dvdDTO1, dvdDTO2);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldReturnAnEmptyListWhenNoDvdsWereFound(String searchTitle) {
        //Act
        List<DvdDTO> actual = underTest.findDvds(searchTitle);

        //Assert
        assertThat(actual).isEmpty();
    }

    @Test
    void shouldDeleteDvdAndCache() {
        // Arrange
        Dvd dvd = new Dvd(
                UUID.randomUUID(),
                "Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5
        );
        dvd = dvdRepository.createDvd(dvd);

        // Act
        underTest.deleteDvd(dvd.getId().toString());
        String id = dvd.getId().toString();

        // Assert
        assertThat(redisTemplate().opsForHash().hasKey(CACHE_NAME, dvd.getId().toString())).isFalse();
        assertThatThrownBy(() -> underTest.findDvdById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dvd was not found with id: " + id);
    }

    @Test
    void shouldThrowResourceNotFoundExceptionWhenDeletingNonExistingDvd() {
        //Arrange
        String id = UUID.randomUUID().toString();

        //Act Assert
        assertThatThrownBy(() -> underTest.deleteDvd(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Dvd was not found with id: " + id);
    }

    private DvdCreateRequest generateDvd() {
        return new DvdCreateRequest(
                "Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5);
    }

    private List<DvdCreateRequest> generateDvds() {
        DvdCreateRequest createRequest = new DvdCreateRequest(
                "Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5);

        DvdCreateRequest createRequest1 = new DvdCreateRequest(
                "Lord of the Rings: The Two Towers",
                DvdGenre.ADVENTURE,
                5);

        return List.of(createRequest, createRequest1);
    }
}

