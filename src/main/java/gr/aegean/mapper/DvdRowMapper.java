package gr.aegean.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import gr.aegean.entity.Dvd;
import gr.aegean.model.dvd.DvdGenre;

import org.springframework.jdbc.core.RowMapper;


public class DvdRowMapper implements RowMapper<Dvd> {

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
