package gr.aegean.mapper.row;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

import gr.aegean.entity.Dvd;
import gr.aegean.mapper.DvdRowMapper;
import gr.aegean.model.dvd.DvdGenre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;


class DvdRowMapperTest {
    private DvdRowMapper underTest;

    @BeforeEach
    void setup() {
        underTest = new DvdRowMapper();
    }

    @Test
    void shouldMapRowToDvd() throws SQLException {
        try(ResultSet resultSet = mock(ResultSet.class)) {
            //Arrange
            UUID id = UUID.randomUUID();
            Dvd expected = new Dvd(id, "Inception", DvdGenre.SCIENCE_FICTION, 3);

            when(resultSet.getString("id")).thenReturn(id.toString());
            when(resultSet.getString("title")).thenReturn("Inception");
            when(resultSet.getString("genre")).thenReturn("SCIENCE_FICTION");
            when(resultSet.getInt("quantity")).thenReturn(3);

            //Act
            Dvd actual = underTest.mapRow(resultSet, 1);

            //Assert
            assertThat(actual).isEqualTo(expected);
        }
    }
}
