package gr.aegean.repository;

import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdGenre;
import gr.aegean.mapper.DvdRowMapper;
import gr.aegean.exception.DvdNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Repository class for handling Dvd database operations.
 */
@Repository
@RequiredArgsConstructor
public class DvdRepository {
    private final JdbcTemplate jdbcTemplate;

    /**
     * Creates a new DVD into the database.
     *
     * @param dvd the DVD to be created.
     * @return the newly created DVD object with its ID set.
     */
    public Dvd createDvd(Dvd dvd) {
        if (checkDuplicateTitle(dvd.getTitle())) {
            throw new DuplicateResourceException("Dvd already exists");
        }

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

    /**
     * Retrieves a DVD from the database by its ID.
     *
     * @param dvdID the ID of the DVD to retrieve.
     * @return the retrieved DVD
     * @throws DvdNotFoundException if no DVD was found with the provided ID.
     */
    public Dvd findDvdByID(String dvdID) {
        final String sql = "SELECT id, title, genre, quantity FROM dvd WHERE id = CAST(? AS uuid)";
        Dvd dvd;

        try {
            dvd = jdbcTemplate.queryForObject(sql, new DvdRowMapper(), dvdID);
        } catch (EmptyResultDataAccessException erd) {
            throw new DvdNotFoundException("No DVD was found with the provided id");
        }

        return dvd;
    }

    /**
     * Retrieves DVDs from the database whose title contains the provided search string.
     *
     * @param title the search string to use when querying the database.
     * @return a list of DVDs whose title contains the search string.
     * @throws DvdNotFoundException if no DVDs were found with the provided string.
     */
    public List<Dvd> findDvdsByTitle(String title) {
        final String sql = "SELECT id, title, genre, quantity FROM dvd WHERE LOWER(title) LIKE ?";
        final String searchTitle = "%" + title.toLowerCase() + "%";

        List<Dvd> dvds = jdbcTemplate.query(sql, new DvdRowMapper(), searchTitle);

        if (dvds.isEmpty()) {
            throw new DvdNotFoundException("No DVDs were found with the provided title");
        }

        return dvds;
    }

    /**
     * Retrieves all DVDs in the database.
     *
     * @return a list of all DVDs in the database.
     * @throws DvdNotFoundException if no DVDs are found in the database.
     */
    public List<Dvd> findAllDvds() {
        final String sql = "SELECT id, title, genre, quantity FROM dvd";

        List<Dvd> dvds = jdbcTemplate.query(sql, new DvdRowMapper());

        if (dvds.isEmpty()) {
            throw new DvdNotFoundException("No DVDs were found");
        }

        return dvds;
    }

    /**
     * Updates the genre of a DVD with the provided ID.
     *
     * @param dvdID the ID of the DVD to update.
     * @param genre the new genre to set for the DVD.
     * @return the updated dvd.
     * @throws DvdNotFoundException if no DVD was found with the provided ID.
     */
    public Dvd updateDvdGenre(String dvdID, DvdGenre genre) {
        final String sql = "UPDATE dvd SET genre = CAST(? AS genre) WHERE id = ?";
        UUID uuid = UUID.fromString(dvdID);

        int update = jdbcTemplate.update(sql, genre.name(), uuid);

        if (update != 1) {
            throw new DvdNotFoundException("No DVD was found with the provided id");
        }

        return findDvdByID(dvdID);
    }

    /**
     * Updates the quantity of a DVD with the provided ID.
     *
     * @param dvdID    the ID of the DVD to update.
     * @param quantity the new quantity to set for the DVD.
     * @return the updated dvd.
     * @throws DvdNotFoundException if no DVD was found with the provided ID.
     */
    public Dvd updateDvdQuantity(String dvdID, Integer quantity) {
        final String sql = "UPDATE dvd SET quantity = ? WHERE id = ?";
        UUID uuid = UUID.fromString(dvdID);

        int update = jdbcTemplate.update(sql, quantity, uuid);

        if (update != 1) {
            throw new DvdNotFoundException("No DVD was found with the provided id");
        }

        return findDvdByID(dvdID);
    }

    /**
     * Deletes a DVD from the database with the provided ID.
     *
     * @param dvdID the ID of the DVD to delete.
     * @throws DvdNotFoundException if no DVD was found with the provided ID.
     */
    public void deleteDvd(String dvdID) {
        final String sql = "DELETE FROM dvd WHERE id = ?";
        UUID uuid = UUID.fromString(dvdID);

        int update = jdbcTemplate.update(sql, uuid);

        if (update != 1) {
            throw new DvdNotFoundException("No DVD was found with the provided id");
        }
    }

    public void deleteAllDvds() {
        final String sql = "DELETE FROM dvd";
        jdbcTemplate.update(sql);
    }

    /**
     * Checks if a DVD with the specified title already exists in the database.
     *
     * @param title the title to check for duplicates.
     * @return true if a DVD with the specified title already exists.
     */
    private boolean checkDuplicateTitle(String title) {
        final String sql = "SELECT COUNT(*) FROM dvd WHERE title = ?";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, title);

        return count != null && count > 0;
    }
}