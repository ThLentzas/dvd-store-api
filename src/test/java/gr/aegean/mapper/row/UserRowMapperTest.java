package gr.aegean.mapper.row;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import gr.aegean.entity.User;
import gr.aegean.mapper.UserRowMapper;
import gr.aegean.model.user.UserRole;

import org.junit.jupiter.api.BeforeEach;

import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.SQLException;


class UserRowMapperTest {
    private UserRowMapper underTest;

    @BeforeEach
    void setup() {
        underTest = new UserRowMapper();
    }

    @Test
    void shouldMapRowToUser() throws SQLException {
        try(ResultSet resultSet = mock(ResultSet.class)) {
            // Arrange
            User expected =new User(
                    1,
                    "Test",
                    "Test",
                    "test@example.com",
                    "Igw4UQAlfX$E",
                    UserRole.ROLE_EMPLOYEE);

            when(resultSet.getInt("id")).thenReturn(1);
            when(resultSet.getString("first_name")).thenReturn("Test");
            when(resultSet.getString("last_name")).thenReturn("Test");
            when(resultSet.getString("email")).thenReturn("test@example.com");
            when(resultSet.getString("password")).thenReturn("Igw4UQAlfX$E");
            when(resultSet.getString("role")).thenReturn(UserRole.ROLE_EMPLOYEE.name());

            // Act
            User actual = underTest.mapRow(resultSet, 1);

            // Assert
            assertThat(actual).isEqualTo(expected);
        }
    }
}
