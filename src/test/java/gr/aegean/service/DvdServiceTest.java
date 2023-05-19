package gr.aegean.service;

import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.exception.DvdNotFoundException;
import gr.aegean.exception.InvalidDVDException;
import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdDTO;
import gr.aegean.mapper.DvdDTOMapper;
import gr.aegean.model.dvd.DvdGenre;
import gr.aegean.repository.DvdRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class DvdServiceTest extends AbstractTestContainers {
    @Mock
    private DvdDTOMapper dvdDTOMapper;
    private DvdRepository dvdRepository;
    private final String CACHE_NAME = "dvds";
    private DvdService underTest;

    @BeforeEach()
    void setup() {
        dvdRepository = new DvdRepository(getJdbcTemplate());
        dvdRepository.deleteAllDvds();

        underTest = new DvdService(dvdRepository, redisTemplate(), dvdDTOMapper);
        redisTemplate().delete(CACHE_NAME);
    }

    @Test
    void shouldCreateDvdAndStoreItInCache() {
        //Arrange
        Dvd initialDvd = new Dvd("Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5);

        when(dvdDTOMapper.convert(initialDvd)).thenAnswer(invocation -> {
            Dvd createdDvd = invocation.getArgument(0);
            UUID generatedID = createdDvd.getId();

            return new DvdDTO(generatedID, initialDvd.getTitle(), initialDvd.getGenre(),
                    initialDvd.getQuantity(), new ArrayList<>());
        });

        //Act
        DvdDTO createdDvdDTO = underTest.createDvd(initialDvd);

        //Assert
        assertThat(createdDvdDTO).isNotNull();
        assertThat(redisTemplate().opsForHash()
                .hasKey(CACHE_NAME, createdDvdDTO.id().toString()))
                .isTrue();
        assertThat(redisTemplate().opsForHash()
                .get(CACHE_NAME, createdDvdDTO.id().toString()))
                .isEqualTo(initialDvd);

        verify(dvdDTOMapper, times(1)).convert(initialDvd);
    }

    @Test
    void shouldThrowDuplicateResourceExceptionIfDvdAlreadyExists() {
        // Arrange
        Dvd dvd = new Dvd("Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5);
        Dvd duplicateDvd = new Dvd("Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5);

        //Act
        underTest.createDvd(dvd);

        // Assert
        assertThatThrownBy(() -> underTest.createDvd(duplicateDvd))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Dvd already exists");
    }

    @Test
    void shouldThrowInvalidDvdExceptionWhenDvdIsNull() {
        assertThatThrownBy(() -> underTest.createDvd(null))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("No information was provided for the dvd");
    }

    @Test
    void shouldThrowInvalidDvdExceptionWhenTitleIsNull() {
        //Arrange
        Dvd nullTitleDvd = new Dvd(null, DvdGenre.ADVENTURE, 5);

        //Act Assert
        assertThatThrownBy(() -> underTest.createDvd(nullTitleDvd))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("No title was provided");
    }

    @Test
    void shouldThrowInvalidDvdExceptionWhenTitleIsEmpty() {
        //Arrange
        Dvd emptyTitleDvd = new Dvd(null, DvdGenre.ADVENTURE, 5);

        //Act Assert
        assertThatThrownBy(() -> underTest.createDvd(emptyTitleDvd))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("No title was provided");
    }

    @Test
    void shouldThrowInvalidDVDExceptionWhenTitleContainsOnlyInvalidCharacters() {
        //Arrange
        Dvd titleContainsOnlyInvalidCharactersDvd = new Dvd("!@#*(){}[]_^~`;?/<>,\\|\"",
                DvdGenre.ADVENTURE, 5);

        //Act Assert
        assertThatThrownBy(() -> underTest.createDvd(titleContainsOnlyInvalidCharactersDvd))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("No title was provided");

    }

    @Test
    void shouldThrowInvalidDVDExceptionWhenQuantityIsNull() {
        //Arrange
        Dvd nullQuantityDvd = new Dvd("Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, null);

        //Act Assert
        assertThatThrownBy(() -> underTest.createDvd(nullQuantityDvd))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("No quantity was provided");
    }

    @Test
    void shouldThrowInvalidDVDExceptionWhenQuantityIsNegative() {
        //Arrange
        Dvd negativeQuantityDvd = new Dvd("Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, -1);

        //Act Assert
        assertThatThrownBy(() -> underTest.createDvd(negativeQuantityDvd))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("The quantity can't be negative or 0");
    }

    @Test
    void shouldThrowInvalidDVDExceptionWhenQuantityIsZero() {
        //Arrange
        Dvd zeroQuantityDvd = new Dvd("Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 0);

        //Act Assert
        assertThatThrownBy(() -> underTest.createDvd(zeroQuantityDvd))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("The quantity can't be negative or 0");
    }

    @Test
    void shouldThrowDvdInvalidExceptionWhenUpdatingNeitherQuantityNorGenre() {
        //Arrange
        UUID id = UUID.randomUUID();

        //Act Assert
        assertThatThrownBy(() -> underTest.updateDvd(id.toString(), null, null))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("Either quantity or genre must be provided");
    }

    @Test
    void shouldThrowDvdInvalidExceptionWhenUpdatingBothQuantityAndGenre() {
        //Arrange
        UUID id = UUID.randomUUID();
        Integer quantity = 1;
        DvdGenre genre = DvdGenre.ADVENTURE;

        //Act Assert
        assertThatThrownBy(() -> underTest.updateDvd(id.toString(), quantity, genre))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("You can update one property at a time");
    }

    @Test
    void shouldFindDvdByIDFromCache() {
        //Arrange
        UUID id = UUID.randomUUID();
        Dvd initialDvd = new Dvd(id, "Lord, of the Rings: The Fellowship of the Ring", DvdGenre.ADVENTURE, 5);
        DvdDTO expectedDvdDTO = new DvdDTO(id, "Lord, of the Rings: The Fellowship of the Ring", DvdGenre.ADVENTURE, 5,
                new ArrayList<>());

        redisTemplate().opsForHash().put(CACHE_NAME, initialDvd.getId().toString(), initialDvd);

        when(dvdDTOMapper.convert(any(Dvd.class))).thenReturn(expectedDvdDTO);

        //Act
        DvdDTO retrievedDvdDTO = underTest.findDvdByID(id.toString());

        //Assert
        assertThat(retrievedDvdDTO).isNotNull();
        assertThat(retrievedDvdDTO).isEqualTo(expectedDvdDTO);

        verify(dvdDTOMapper, times(1)).convert(any(Dvd.class));
    }

    @Test
    void shouldFindDvdByIDFromRepositoryAndUpdateCache() {
        //Arrange
        Dvd initialDvd = new Dvd("Lord, of the Rings: The Fellowship of the Ring", DvdGenre.ADVENTURE, 5);
        Dvd createdDvd = dvdRepository.createDvd(initialDvd);
        DvdDTO expectedDvdDTO = new DvdDTO(createdDvd.getId(), "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5, new ArrayList<>());

        when(dvdDTOMapper.convert(any(Dvd.class))).thenReturn(expectedDvdDTO);

        //Act
        DvdDTO retrievedDvdDTO = underTest.findDvdByID(createdDvd.getId().toString());

        //Assert
        assertThat(retrievedDvdDTO).isNotNull();
        assertThat(retrievedDvdDTO).isEqualTo(expectedDvdDTO);
        assertThat(redisTemplate().opsForHash().get(CACHE_NAME, initialDvd.getId().toString())).isEqualTo(initialDvd);

        verify(dvdDTOMapper, times(1)).convert(any(Dvd.class));
    }

    @Test
    void shouldThrowDvdNotFoundExceptionWhenFindingNonExistingDvdById() {
        //Arrange
        String nonExistingID = UUID.randomUUID().toString();

        //Act and Assert
        assertThatThrownBy(() -> underTest.findDvdByID(nonExistingID))
                .isInstanceOf(DvdNotFoundException.class)
                .hasMessage("No DVD was found with the provided id");
    }

    @Test
    void shouldFindDvdsByTitle() {
        //Arrange
        String searchTitle = "Lord";

        Dvd dvd1 = new Dvd("Lord, of the Rings: The Fellowship of the Ring", DvdGenre.ADVENTURE, 5);
        Dvd dvd2 = new Dvd("Lord, of the Rings: The Two Towers", DvdGenre.ADVENTURE, 5);

        Dvd createdDvd1 = dvdRepository.createDvd(dvd1);
        Dvd createdDvd2 = dvdRepository.createDvd(dvd2);

        DvdDTO expectedDvdDTO1 = new DvdDTO(createdDvd1.getId(), "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5, new ArrayList<>());
        DvdDTO expectedDvdDTO2 = new DvdDTO(createdDvd2.getId(), "Lord, of the Rings: The Two Towers",
                DvdGenre.ADVENTURE, 5, new ArrayList<>());

        when(dvdDTOMapper.convert(any(Dvd.class))).thenReturn(expectedDvdDTO1, expectedDvdDTO2);

        //Act
        List<DvdDTO> retrievedDvdsDTO = underTest.findDvds(searchTitle);

        //Assert
        assertThat(retrievedDvdsDTO).isNotNull();
        assertThat(retrievedDvdsDTO).hasSize(2);
        assertThat(retrievedDvdsDTO).containsExactlyInAnyOrder(expectedDvdDTO1, expectedDvdDTO2);

        verify(dvdDTOMapper, times(2)).convert(any(Dvd.class));
    }

    @Test
    void shouldThrowDvdNotFoundExceptionWhenNoDvdsMatchTitle() {
        //Arrange
        String searchTitle = "Lord";

        //Act Assert
        assertThatThrownBy(() -> underTest.findDvds(searchTitle))
                .isInstanceOf(DvdNotFoundException.class)
                .hasMessage("No DVDs were found with the provided title");
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void shouldFindAllDvdsWhenTitleIsNullOrEmpty(String searchTitle) {
        //Arrange
        Dvd dvd1 = new Dvd("Lord, of the Rings: The Fellowship of the Ring", DvdGenre.ADVENTURE, 5);
        Dvd dvd2 = new Dvd("Lord, of the Rings: The Two Towers", DvdGenre.ADVENTURE, 5);

        Dvd createdDvd1 = dvdRepository.createDvd(dvd1);
        Dvd createdDvd2 = dvdRepository.createDvd(dvd2);

        DvdDTO expectedDvdDTO1 = new DvdDTO(createdDvd1.getId(), "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5, new ArrayList<>());
        DvdDTO expectedDvdDTO2 = new DvdDTO(createdDvd2.getId(), "Lord, of the Rings: The Two Towers",
                DvdGenre.ADVENTURE, 5, new ArrayList<>());

        when(dvdDTOMapper.convert(any(Dvd.class))).thenReturn(expectedDvdDTO1, expectedDvdDTO2);

        //Act
        List<DvdDTO> retrievedDvdsDTO = underTest.findDvds(searchTitle);

        //Assert
        assertThat(retrievedDvdsDTO).isNotNull();
        assertThat(retrievedDvdsDTO).hasSize(2);
        assertThat(retrievedDvdsDTO).containsExactlyInAnyOrder(expectedDvdDTO1, expectedDvdDTO2);

        verify(dvdDTOMapper, times(2)).convert(any(Dvd.class));
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void shouldThrowDvdNotFoundExceptionIfWhenNoDvdsExist(String searchTitle) {
        assertThatThrownBy(() -> underTest.findDvds(searchTitle))
                .isInstanceOf(DvdNotFoundException.class)
                .hasMessage("No DVDs were found");
    }

    @Test
    void shouldUpdateDvdQuantityAndCache() {
        // Arrange
        Dvd initialDvd = new Dvd( "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5);

        Dvd toBeUpdatedDvd = dvdRepository.createDvd(initialDvd);
        DvdDTO expectedDvdDTO = new DvdDTO(toBeUpdatedDvd.getId(), "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 7, new ArrayList<>());

        when(dvdDTOMapper.convert(any(Dvd.class))).thenReturn(expectedDvdDTO);

        //Act
        DvdDTO updatedDvdDTO = underTest.updateDvd(toBeUpdatedDvd.getId().toString(), 7, null);
        toBeUpdatedDvd.setQuantity(7);

        //Assert
        assertThat(updatedDvdDTO).isNotNull();
        assertThat(updatedDvdDTO).isEqualTo(expectedDvdDTO);
        assertThat(redisTemplate().opsForHash().get(CACHE_NAME, updatedDvdDTO.id().toString())).
                isEqualTo(toBeUpdatedDvd);

        verify(dvdDTOMapper, times(1)).convert(any(Dvd.class));
    }

    @Test
    void shouldThrowDvdNotFoundExceptionWhenUpdatingQuantityOfNonExistingDvd() {
        //Arrange
        String nonExistingID = UUID.randomUUID().toString();

        //Act Assert
        assertThatThrownBy(() -> underTest.updateDvd(nonExistingID, 6, null))
                .isInstanceOf(DvdNotFoundException.class)
                .hasMessage("No DVD was found with the provided id");
    }

    @Test
    void shouldUpdateDvdGenreAndCache() {
        // Arrange
        Dvd initialDvd = new Dvd( "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5);

        Dvd toBeUpdatedDvd = dvdRepository.createDvd(initialDvd);
        DvdDTO expectedDvdDTO = new DvdDTO(initialDvd.getId(), "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.THRILLER, 7, new ArrayList<>());

        when(dvdDTOMapper.convert(any(Dvd.class))).thenReturn(expectedDvdDTO);

        //Act
        DvdDTO updatedDvdDTO = underTest.updateDvd(initialDvd.getId().toString(), null, DvdGenre.THRILLER);
        initialDvd.setGenre(DvdGenre.THRILLER);

        //Assert
        assertThat(updatedDvdDTO).isNotNull();
        assertThat(updatedDvdDTO).isEqualTo(expectedDvdDTO);
        assertThat(redisTemplate().opsForHash().get(CACHE_NAME, updatedDvdDTO.id().toString())).
                isEqualTo(toBeUpdatedDvd);

        verify(dvdDTOMapper, times(1)).convert(any(Dvd.class));
    }

    @Test
    void shouldThrowDvdNotFoundExceptionWhenUpdatingGenreOfNonExistingDvd() {
        //Arrange
        String nonExistingID = UUID.randomUUID().toString();
        DvdGenre dvdGenre = DvdGenre.SOCIOLOGICAL;

        //Act Assert
        assertThatThrownBy(() -> underTest.updateDvd(nonExistingID, null, dvdGenre))
                .isInstanceOf(DvdNotFoundException.class)
                .hasMessage("No DVD was found with the provided id");
    }

    @Test
    void shouldDeleteDvdAndCache() {
        // Arrange
        Dvd initialDvd = new Dvd( "Lord, of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE, 5);

        Dvd toBeDeletedDvd = dvdRepository.createDvd(initialDvd);

        // Act
        underTest.deleteDvd(toBeDeletedDvd.getId().toString());

        // Assert
        assertThat(redisTemplate().opsForHash().hasKey(CACHE_NAME, toBeDeletedDvd.getId().toString())).isFalse();
        assertThatThrownBy(() -> dvdRepository.findDvdByID(toBeDeletedDvd.getId().toString()))
                .isInstanceOf(DvdNotFoundException.class)
                .hasMessage("No DVD was found with the provided id");
    }

    @Test
    void shouldThrowDvdNotFoundExceptionWhenDeletingNonExistingDvd() {
        //Arrange
        String nonExistingID = UUID.randomUUID().toString();

        //Act Assert
        assertThatThrownBy(() -> underTest.deleteDvd(nonExistingID))
                .isInstanceOf(DvdNotFoundException.class)
                .hasMessage("No DVD was found with the provided id");
    }
}

