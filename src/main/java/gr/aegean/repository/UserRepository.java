package gr.aegean.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

import gr.aegean.mapper.UserRowMapper;
import gr.aegean.entity.User;

import lombok.RequiredArgsConstructor;


@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public void registerUser(User user) {
        final String sql = "INSERT INTO app_user (" +
                "first_name, " +
                "last_name, email, " +
                "password, role) VALUES (?, ?, ?, ?, " + "CAST (? AS role))";
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

        if (insert == 1) {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("id")) {
                user.setId((Integer) keys.get("id"));
            }
        }
    }

    /**
     * This method will be used by UsersDetailsService for the user authentication.
     */
    public Optional<User> findUserByEmail(String email) {
        final String sql = "SELECT id, first_name, last_name, email, password, role FROM app_user WHERE email = ?";
        User user;

        try {
            user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), email);

            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException erda) {
            return Optional.empty();
        }
    }

    /*
        Emails are not case-sensitive. "test@example.com" and "Test@example.com" will be considered as duplicates
     */
    public boolean existsUserWithEmail(String email) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM app_user WHERE email ILIKE ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, email));
    }

    public void deleteAllUsers() {
        final String sql = "DELETE FROM app_user";
        jdbcTemplate.update(sql);
    }
}
