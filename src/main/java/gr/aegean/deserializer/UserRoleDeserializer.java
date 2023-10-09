package gr.aegean.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import gr.aegean.model.user.UserRole;

import java.io.IOException;

/*
    Custom deserializer for converting JSON string values to UserRole enum values. This deserializer is needed to
    correctly handle cases where the provided role is in different cases (" cuStoMer", "CustomeR  "),
    while our enum value is in uppercase.
 */
public class UserRoleDeserializer extends JsonDeserializer<UserRole> {

    @Override
    public UserRole deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser
                .getValueAsString()
                .trim()
                .toUpperCase();

        try {
            value = "ROLE_" + value;

            return UserRole.valueOf(value);
        } catch (IllegalArgumentException iae) {
            throw new IllegalArgumentException("Invalid user role");
        }
    }
}
