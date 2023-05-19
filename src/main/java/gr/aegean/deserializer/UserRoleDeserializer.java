package gr.aegean.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import gr.aegean.exception.BadCredentialsException;
import gr.aegean.model.user.UserRole;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * Custom deserializer for converting JSON string values to UserRole enum values.
 */
public class UserRoleDeserializer extends JsonDeserializer<UserRole> {

    /**
     * Converts the provided JSON string value to a DVDGenre enum value.
     *
     * @param parser the JsonParser object used to parse the JSON value
     * @param context the DeserializationContext object for the deserialization process
     * @return the corresponding DVDGenre enum value
     * @throws BadCredentialsException if the provided genre does not match one of the values in
     * the enumeration of valid DVD genres.
     */
    @Override
    public UserRole deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString().toUpperCase();

        try {
            value = "ROLE_" + value;

            return UserRole.valueOf(value);
        } catch (IllegalArgumentException iae) {
            throw new BadCredentialsException("Invalid user role");
        }
    }
}
