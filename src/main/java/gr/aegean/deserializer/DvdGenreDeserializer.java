package gr.aegean.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import gr.aegean.exception.InvalidDVDException;
import gr.aegean.model.dvd.DvdGenre;

import java.io.IOException;

/**
 * Custom deserializer for converting JSON string values to DVDGenre enum values.
 */
public class DvdGenreDeserializer extends JsonDeserializer<DvdGenre>{

    /**
     * Converts the provided JSON string value to a DVDGenre enum value.
     *
     * @param parser the JsonParser object used to parse the JSON value
     * @param context the DeserializationContext object for the deserialization process
     * @return the corresponding UserRole enum value
     * @throws InvalidDVDException if the provided user role does not match one of the values in
     * the enumeration of valid User roles.
     */
    @Override
    public DvdGenre deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString().toUpperCase();

        try {
            return DvdGenre.valueOf(value);
        } catch (IllegalArgumentException iae) {
            throw new InvalidDVDException("Invalid dvd genre");
        }
    }
}