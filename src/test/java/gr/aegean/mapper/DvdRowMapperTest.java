package gr.aegean.mapper;

import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdGenre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

public class DvdRowMapperTest {
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

            when(resultSet.getString("id")).thenReturn(id.toString());
            when(resultSet.getString("title")).thenReturn("Inception");
            when(resultSet.getString("genre")).thenReturn("SCIENCE_FICTION");
            when(resultSet.getInt("quantity")).thenReturn(3);

            //Act
            Dvd dvd = underTest.mapRow(resultSet, 1);

            //Assert
            assertThat(dvd).isNotNull();
            assertThat(dvd.getId()).isEqualTo(id);
            assertThat(dvd.getTitle()).isEqualTo("Inception");
            assertThat(dvd.getGenre()).isEqualTo(DvdGenre.valueOf("SCIENCE_FICTION"));
            assertThat(dvd.getQuantity()).isEqualTo(3);
        }
    }
}
