package gr.aegean.mapper;

import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdGenre;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * DVDRowMapper implementation to map rows from the result set to DVD objects.
 */
public class DvdRowMapper implements RowMapper<Dvd> {

    /**
     * Maps a row from the result set to a DVD object.
     *
     * @param resultSet the result set containing the data to map.
     * @param rowNum the number of the current row.
     * @return DVD object mapped from the result set.
     * @throws SQLException  if there's an error accessing the result set.
     */
    @Override
    public Dvd mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        UUID id = UUID.fromString(resultSet.getString("id"));

        return new Dvd(
                id,
                resultSet.getString("title"),
                DvdGenre.valueOf(resultSet.getString("genre")),
                resultSet.getInt("quantity")
        );
    }
}
