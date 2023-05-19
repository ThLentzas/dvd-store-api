package gr.aegean.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import gr.aegean.exception.InvalidDVDException;
import gr.aegean.model.dvd.DvdGenre;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


public class DvdGenreDeserializerTest {
    private DvdGenreDeserializer dvdGenreDeserializer;

    @BeforeEach
    void setup() {
        dvdGenreDeserializer = new DvdGenreDeserializer();
    }

    @Test
    void shouldDeserializeGenre() throws IOException {
        //Arrange
        String genre = "THRILLER";
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(genre.toUpperCase());

        //Act
        DvdGenre dvdGenre = dvdGenreDeserializer.deserialize(parser, context);

        //Assert
        assertThat(dvdGenre).isEqualTo(DvdGenre.THRILLER);
    }

    @Test
    void shouldDeserializeGenreIgnoringCase() throws IOException {
        //Arrange
        String genre = "thRIlLER";
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(genre.toUpperCase());

        //Act
        DvdGenre dvdGenre = dvdGenreDeserializer.deserialize(parser, context);

        //Assert
        assertThat(dvdGenre).isEqualTo(DvdGenre.THRILLER);
        verify(parser, times(1)).getValueAsString();
    }

    @Test
    void shouldThrowInvalidDvdExceptionWhenGenreIsInvalid() throws IOException {
        //Arrange
        String invalidGenre = "invalidGenre";
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(invalidGenre.toUpperCase());

        //Act Assert
        assertThatThrownBy(() -> dvdGenreDeserializer.deserialize(parser, context))
                .isInstanceOf(InvalidDVDException.class)
                .hasMessage("Invalid dvd genre");
    }


}
