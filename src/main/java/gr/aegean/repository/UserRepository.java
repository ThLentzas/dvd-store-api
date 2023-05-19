package gr.aegean.repository;

import gr.aegean.exception.BadCredentialsException;
import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.mapper.UserRowMapper;
import gr.aegean.model.user.User;
import gr.aegean.model.user.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

/**
 * Repository class for handling User database operations.
 */
@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Inserts a new user to the database.
     *
     * @param user the user to be inserted.
     * @return the ID of the newly inserted user that will be used for the URI.
     * @throws DuplicateResourceException if the provided email already exists in the database.
     */
    public Integer registerUser(User user) {
        if(checkDuplicateEmail(user.getEmail())) {
            throw new DuplicateResourceException("The provided email already exists");
        }

        final String sql = "INSERT INTO app_user (first_name, last_name, email, password, role) VALUES (?, ?, ?, ?, " +
                "CAST (? AS role))";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int insert = jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getFirstname());
            preparedStatement.setString(2, user.getLastname());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setString(4, user.getPassword());
            preparedStatement.setString(5, user.getRole().name());

            return preparedStatement;
        }, keyHolder);

        Integer id = null;
        if (insert == 1) {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("id")) {
                id = (Integer) keys.get("id");
            }
        }

        return id;
    }

    /**
     * Searches for a user by their email in the database and returns a UserPrincipal which contains the
     * user's email, password, and role.
     *
     * @param email the email of the user to be searched for in the database.
     * @return a UserPrincipal object representing the user found in the database
     * @throws BadCredentialsException if the provided email does not exist in the database.
     */
    public UserPrincipal findUserByEmail(String email) {
        final String sql = "SELECT email, password, role FROM app_user WHERE email = ?";
        User user;
        UserPrincipal userPrincipal = null;

        try {
            user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);
            if(user != null) {
                userPrincipal = new UserPrincipal(user);
            }

        } catch (EmptyResultDataAccessException erda) {
            throw new BadCredentialsException("Username or password is incorrect");
        }

        return userPrincipal;
    }

    /**
     * Check if the provided email already exists in the database.
     *
     * @param email the email to be checked.
     * @return true if the email already exists, false otherwise.
     */
    private boolean checkDuplicateEmail(String email) {
        final String sql = "SELECT COUNT(*) FROM app_user WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);

        return count != null && count > 0;
    }

    public void deleteAllUsers() {
        final String sql = "DELETE FROM app_user";
        jdbcTemplate.update(sql);
    }

}
