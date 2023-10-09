package gr.aegean.service;

import gr.aegean.AbstractUnitTest;
import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.entity.User;
import gr.aegean.model.user.UserRole;
import gr.aegean.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


class UserServiceTest extends AbstractUnitTest {
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
    void shouldRegisterUser() {
        //Arrange
        User expected = generateUser();

        //Act
        underTest.registerUser(expected);

        //Assert
        userRepository.findUserByEmail(expected.getEmail())
                .ifPresent(actual -> assertThat(actual).isEqualTo(expected));
    }

    @Test
    void shouldThrowDuplicateResourceExceptionIfEmailAlreadyExists() {
        //Arrange
        User user = generateUser();
        User duplicateEmailUser = generateUser();

        //Act
        underTest.registerUser(user);

        //Assert
        assertThatThrownBy(() -> underTest.registerUser(duplicateEmailUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("The provided email already exists");
    }

    private User generateUser() {
        return new User(
                "Test",
                "Test",
                "test@example.com",
                passwordEncoder.encode("test"),
                UserRole.ROLE_EMPLOYEE);
    }
}
