package gr.aegean.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import gr.aegean.exception.BadCredentialsException;

import gr.aegean.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class UserRoleDeserializerTest {
    private UserRoleDeserializer userRoleDeserializer;

    @BeforeEach
    void setup() {
        userRoleDeserializer = new UserRoleDeserializer();
    }

    @Test
    void shouldDeserializeRole() throws IOException {
        //Arrange
        String role = "EMPLOYEE";
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(role.toUpperCase());

        //Act
        UserRole userRole = userRoleDeserializer.deserialize(parser, context);

        //Assert
        assertThat(userRole).isEqualTo(UserRole.ROLE_EMPLOYEE);
    }

    @Test
    void shouldDeserializeRoleIgnoringCase() throws IOException {
        //Arrange
        String role = "EmPloYeE";
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(role.toUpperCase());

        //Act
        UserRole userRole = userRoleDeserializer.deserialize(parser, context);

        //Assert
        assertThat(userRole).isEqualTo(UserRole.ROLE_EMPLOYEE);
    }

    @Test
    void shouldThrowInvalidDvdExceptionWhenGenreIsInvalid() throws IOException {
        //Arrange
        String invalidRole = "invalidRole";
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(invalidRole.toUpperCase());

        //Act Assert
        assertThatThrownBy(() -> userRoleDeserializer.deserialize(parser, context))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid user role");
    }
}
