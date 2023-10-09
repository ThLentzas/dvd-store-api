package gr.aegean.deserializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import gr.aegean.model.dvd.DvdGenre;

import java.io.IOException;


class DvdGenreDeserializerTest {
    private DvdGenreDeserializer underTest;

    @BeforeEach
    void setup() {
        underTest = new DvdGenreDeserializer();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SCIENCE_FICTION", "sCiENCE_FICTiON", " sCiENCE FICTiON   "})
    void shouldDeserializeGenre(String genre) throws IOException {
        //Arrange
        DvdGenre expected = DvdGenre.SCIENCE_FICTION;
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(genre);

        //Act
        DvdGenre dvdGenre = underTest.deserialize(parser, context);

        //Assert
        assertThat(dvdGenre).isEqualTo(expected);
    }

    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"invalidGenre"})
    void shouldThrowInvalidDvdExceptionWhenGenreIsInvalid(String genre) throws IOException {
        //Arrange
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(genre);

        //Act Assert
        assertThatThrownBy(() -> underTest.deserialize(parser, context))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid dvd genre: " + genre.toUpperCase());
    }
}
