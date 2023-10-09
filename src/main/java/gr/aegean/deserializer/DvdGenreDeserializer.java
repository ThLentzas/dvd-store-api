package gr.aegean.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import gr.aegean.model.dvd.DvdGenre;

import java.io.IOException;

/*
   Custom deserializer for converting JSON string values to DVDGenre enum values. This deserializer is needed to
   correctly handle cases where user provides dvd genre in different cases ("comedY", "COMedy"),
   while our enum value is in uppercase.
 */
public class DvdGenreDeserializer extends JsonDeserializer<DvdGenre>{

    @Override
    public DvdGenre deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser
                .getValueAsString()
                .trim()
                .replaceAll("\\s+", "_")
                .toUpperCase();

        try {
            return DvdGenre.valueOf(value);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Invalid dvd genre: " + value);
        }
    }
}