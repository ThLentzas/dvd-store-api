package gr.aegean.mapper;

import gr.aegean.model.user.User;
import gr.aegean.model.user.UserRole;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserRowMapper implementation to map rows from the result set to User objects.
 */
public class UserRowMapper implements RowMapper<User> {
    /**
     * Maps a row from the result set to a User object.

     * @param resultSet the result set containing the data to map.
     * @param rowNum the number of the current row.
     * @return User object mapped from the result set.
     * @throws SQLException if there's an error accessing the result set.
     */
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {

        return new User(
                resultSet.getString("email"),
                resultSet.getString("password"),
                UserRole.valueOf(resultSet.getString("role"))
        );
    }
}
