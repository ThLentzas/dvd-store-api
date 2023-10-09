package gr.aegean.deserializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

import gr.aegean.model.user.UserRole;

import java.io.IOException;


class UserRoleDeserializerTest {
    private UserRoleDeserializer userRoleDeserializer;

    @BeforeEach
    void setup() {
        userRoleDeserializer = new UserRoleDeserializer();
    }

    @ParameterizedTest
    @ValueSource(strings = {"EMPLOYEE", "EmPloYeE", "   EmPloYeE "})
    void shouldDeserializeRoleRegardlessOfCase(String role) throws IOException {
        // Arrange
        UserRole expected = UserRole.ROLE_EMPLOYEE;
        JsonParser parser = mock(JsonParser.class);
        DeserializationContext context = mock(DeserializationContext.class);

        when(parser.getValueAsString()).thenReturn(role);

        // Act
        UserRole actual = userRoleDeserializer.deserialize(parser, context);

        // Assert
        assertThat(actual).isEqualTo(expected);
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
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid user role");
    }
}
