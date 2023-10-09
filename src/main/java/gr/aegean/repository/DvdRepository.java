package gr.aegean.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import gr.aegean.entity.Dvd;
import gr.aegean.mapper.DvdRowMapper;
import gr.aegean.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;


@Repository
@RequiredArgsConstructor
public class DvdRepository {
    private final JdbcTemplate jdbcTemplate;

    public Dvd createDvd(Dvd dvd) {
        final String sql = "INSERT INTO dvd (title, genre, quantity) VALUES (?, CAST(? AS genre), ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        int create = jdbcTemplate.update(con -> {
            PreparedStatement preparedStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, dvd.getTitle());
            preparedStatement.setString(2, dvd.getGenre().name());
            preparedStatement.setInt(3, dvd.getQuantity());

            return preparedStatement;
        }, keyHolder);

        UUID dvdID;
        if (create == 1) {
            Map<String, Object> keys = keyHolder.getKeys();
            if (keys != null && keys.containsKey("id")) {
                dvdID = (UUID) keys.get("id");
                dvd.setId(dvdID);
            }
        }

        return dvd;
    }

    /*
        queryForObject() will throw EmptyResultDataAccessException when the query is expected to return a single row,
        but no rows are returned and IncorrectResultSizeDataAccessException when more than one row is returned. It will
        the result object of the required type, or null in case of SQL NULL. By using Optional.ofNullable() we ensure an
        empty optional in case queryForObject() returns null
        EmptyResultDataAccessException extends IncorrectResultSizeDataAccessException so by catching the parent class
        we deal with both cases
     */
    public Optional<Dvd> findDvdByID(String dvdID) {
        final String sql = "SELECT id, title, genre, quantity FROM dvd WHERE id = CAST(? AS uuid)";

        try {
            Dvd dvd = jdbcTemplate.queryForObject(sql, new DvdRowMapper(), dvdID);

            return Optional.ofNullable(dvd);
        } catch (IncorrectResultSizeDataAccessException irs) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves DVDs from the database whose title matches or partially matches the provided search string.
     */
    public List<Dvd> findDvdsByTitle(String title) {
        final String sql = "SELECT id, title, genre, quantity FROM dvd WHERE title ILIKE ?";
        final String searchTitle = "%" + title + "%";

        return jdbcTemplate.query(sql, new DvdRowMapper(), searchTitle);
    }

    public List<Dvd> findAllDvds() {
        final String sql = "SELECT id, title, genre, quantity FROM dvd";

        return jdbcTemplate.query(sql, new DvdRowMapper());
    }

    public void updateDvd(Dvd dvd) {
        final String sql = "UPDATE dvd SET genre = CAST(? AS genre), quantity = ? WHERE id = ?";

        jdbcTemplate.update(sql, dvd.getGenre().name(), dvd.getQuantity(), dvd.getId());
    }

    public void deleteDvd(String dvdID) {
        final String sql = "DELETE FROM dvd WHERE id = CAST(? AS uuid)";

        int updated = jdbcTemplate.update(sql, dvdID);
        if (updated != 1) {
            throw new ResourceNotFoundException("Dvd was not found with id: " + dvdID);
        }
    }

    public boolean existsDvdWithTitle(String title) {
        final String sql = "SELECT EXISTS (SELECT 1 FROM dvd WHERE title = ?)";

        return Boolean.TRUE.equals(jdbcTemplate.queryForObject(sql, Boolean.class, title));
    }

    public void deleteAllDvds() {
        final String sql = "DELETE FROM dvd";

        jdbcTemplate.update(sql);
    }
}