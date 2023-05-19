package gr.aegean.mapper;

import gr.aegean.model.user.User;

import gr.aegean.model.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserRowMapperTest {
    private UserRowMapper underTest;

    @BeforeEach
    void setup() {
        underTest = new UserRowMapper();
    }

    @Test
    void shouldMapRowToUserPrincipal() throws SQLException {
        try(ResultSet resultSet = mock(ResultSet.class)) {
            //Arrange
            when(resultSet.getString("email")).thenReturn("test@example.com");
            when(resultSet.getString("password")).thenReturn("password");
            when(resultSet.getString("role")).thenReturn("ROLE_EMPLOYEE");

            //Act
            User user = underTest.mapRow(resultSet, 1);

            //Assert
            assertThat(user).isNotNull();
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPassword()).isEqualTo("password");
            assertThat(user.getRole()).isEqualTo(UserRole.valueOf("ROLE_EMPLOYEE"));
        }
    }
}
