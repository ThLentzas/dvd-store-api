package gr.aegean.mapper.dto;

import gr.aegean.entity.Dvd;
import gr.aegean.mapper.DvdDTOMapper;
import gr.aegean.model.dto.dvd.DvdDTO;
import gr.aegean.model.dvd.DvdGenre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class DvdDTOMapperTest {
    private DvdDTOMapper underTest;

    @BeforeEach
    void setup() {
        underTest = new DvdDTOMapper();
    }

    @Test
    void shouldMapDvdToDvdDTO() {
        //Arrange
        Dvd expected = new Dvd(
                UUID.randomUUID(),
                "The Lord of the Rings: The Fellowship of the Ring",
                DvdGenre.ADVENTURE,
                5
        );

        //Act
        DvdDTO actual = underTest.convert(expected);

        //Assert
        assertThat(actual.id()).isEqualTo(expected.getId());
        assertThat(actual.title()).isEqualTo(expected.getTitle());
        assertThat(actual.genre()).isEqualTo(expected.getGenre());
        assertThat(actual.quantity()).isEqualTo(expected.getQuantity());
    }
}
