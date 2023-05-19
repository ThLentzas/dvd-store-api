package gr.aegean.service;

import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.model.user.User;
import gr.aegean.model.user.UserRole;
import gr.aegean.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserServiceTest extends AbstractTestContainers {
    private UserRepository userRepository;
    private UserService underTest;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setup() {
        userRepository = new UserRepository(getJdbcTemplate());
        underTest = new UserService(userRepository);

        userRepository.deleteAllUsers();
    }

    @Test
    void shouldCreateUserAndReturnTheGeneratedID() {
        //Arrange
        User user = new User("Employee", "Employee", "employee@gmail.com",
                passwordEncoder.encode("test"), UserRole.ROLE_EMPLOYEE);

        //Act
        Integer userID = underTest.registerUser(user);

        //Assert
        assertThat(userID).isNotNull();
    }

    @Test
    void shouldThrowDuplicateResourceExceptionIfEmailAlreadyExists() {
        //Arrange
        User user1 = new User("Employee", "Employee", "employee@gmail.com",
                passwordEncoder.encode("test"), UserRole.ROLE_EMPLOYEE);
        User user2 = new User("Employee", "Employee", "employee@gmail.com",
                passwordEncoder.encode("test"), UserRole.ROLE_EMPLOYEE);

        //Act
        userRepository.registerUser(user1);

        //Assert
        assertThatThrownBy(() -> underTest.registerUser(user2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("The provided email already exists");
    }
}
