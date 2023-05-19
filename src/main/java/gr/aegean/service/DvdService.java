package gr.aegean.service;

import gr.aegean.exception.BadCredentialsException;
import gr.aegean.exception.InvalidDVDException;
import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdDTO;
import gr.aegean.mapper.DvdDTOMapper;
import gr.aegean.model.dvd.DvdGenre;
import gr.aegean.repository.DvdRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for Dvd-related functionality and caching
 */
@Service
@RequiredArgsConstructor
public class DvdService {
    private final DvdRepository dvdRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DvdDTOMapper dvdDTOMapper;

    /**
     * Creates a new DVD to the database and adds it to Redis cache.
     *
     * @param dvd the DVD to be created.
     * @return the DVD data transfer object (DTO) containing the details of the newly created DVD,
     * including its UUID for URI creation.
     * @throws BadCredentialsException if the provided DVD is null or contains invalid information.
     */
    public DvdDTO createDvd(Dvd dvd) {
        if (isDVDValid(dvd)) {
            dvd = dvdRepository.createDvd(dvd);
        }

        redisTemplate.opsForHash().put("dvds", dvd.getId().toString(), dvd);


        return dvdDTOMapper.convert(dvd);
    }

    /**
     * Finds a DVD by its ID. First checks the Redis cache for the DVD with the given ID.
     * If it exists in the cache, returns a DvdDTO. If not, retrieves the data from the database,
     * saves it to the Redis cache and returns a DvdDTO containing the retrieved data.
     *
     * @param dvdID the ID of the DVD to find.
     * @return a DvdDTO containing the retrieved DVD data.
     */
    public DvdDTO findDvdByID(String dvdID) {
        Dvd dvd = (Dvd) redisTemplate.opsForHash().get("dvds", dvdID);

        if (dvd != null) {
            return dvdDTOMapper.convert(dvd);
        }

        dvd = dvdRepository.findDvdByID(dvdID);
        redisTemplate.opsForHash().put("dvds", dvd.getId().toString(), dvd);

        return dvdDTOMapper.convert(dvd);
    }

    /**
     * Retrieves all DVDs from the database or cache.
     * If a non-null and non-empty title is provided, DVDs matching the title are returned.
     * If cached DVDs are available, they are returned. Otherwise, DVDs are retrieved from the database,
     * added to the cache and returned.
     *
     * @param title the title of the DVD to search for. If null or empty, all DVDs will be returned.
     * @return a list of DvdDTOs representing the found DVDs.
     */
    public List<DvdDTO> findDvds(String title) {
        if (title != null && !title.isEmpty()) {
            return findDvdsByTitle(title);
        }

        return findAllDvds();
    }

    /**
     * Updates the quantity or genre of a DVD in the database and updates the corresponding entry in the cache.
     *
     * @param dvdID    the UUID of the DVD to be updated.
     * @param quantity the provided quantity of the DVD.
     * @param genre    the provided genre of the DVD.
     * @return a DvdDTO containing the updated DVD data.
     * @throws InvalidDVDException if both quantity and genre are provided, or if quantity is invalid
     * or the genre is invalid.
     */
    public DvdDTO updateDvd(String dvdID, Integer quantity, DvdGenre genre) {
        if (quantity == null && genre == null) {
            throw new InvalidDVDException("Either quantity or genre must be provided");
        }

        if (quantity != null && genre != null) {
            throw new InvalidDVDException("You can update one property at a time");
        }

        Dvd updatedDvd;
        DvdDTO dvdDTO;
        if(genre != null) {
            updatedDvd = dvdRepository.updateDvdGenre(dvdID, genre);
            dvdDTO = dvdDTOMapper.convert(updatedDvd);
            redisTemplate.opsForHash().put("dvds", dvdID, updatedDvd);

            return dvdDTO;
        }

        if (isQuantityValid(quantity)) {
            updatedDvd = dvdRepository.updateDvdQuantity(dvdID, quantity);
            dvdDTO = dvdDTOMapper.convert(updatedDvd);
            redisTemplate.opsForHash().put("dvds", dvdID, updatedDvd);

            return dvdDTO;
        }

        return null;
    }

    /**
     * Deletes a DVD from the database.
     *
     * @param dvdID the UUID of the dvd to be deleted.
     */
    public void deleteDvd(String dvdID) {
        dvdRepository.deleteDvd(dvdID);
        redisTemplate.opsForHash().delete("dvds", dvdID);
    }

    /**
     * Validates the dvd, by verifying that it contains a valid title, genre and quantity.
     *
     * @param dvd the dvd to be validated.
     * @return true if the dvd is valid.
     * @throws InvalidDVDException if the provided dvd is null, or contains invalid information.
     */
    private boolean isDVDValid(Dvd dvd) {
        if (dvd == null) {
            throw new InvalidDVDException("No information was provided for the dvd");
        }

        boolean isTitleValid = isTitleValid(dvd.getTitle());
        boolean isQuantityValid = isQuantityValid(dvd.getQuantity());

        return isTitleValid && isQuantityValid;
    }

    /**
     * Validates the title, by verifying that it's not null, empty and matches the regex pattern.
     *
     * @param title the title to be validated.
     * @return true if the title is valid.
     * @throws InvalidDVDException if the title is null, empty, or does not meet the specified requirements.
     */
    private boolean isTitleValid(String title) {
        if (title != null) {
            title = title.trim();
            title = title.replaceAll("[^a-zA-Z-\\s+.':-]", "");
        }

        if (title == null || title.isEmpty()) {
            throw new InvalidDVDException("No title was provided");
        }

        return true;
    }

    /**
     * Validates the quantity by verifying that it's not null and a positive integer.
     *
     * @param quantity the quantity to be validated.
     * @return true if the quantity is valid.
     * @throws InvalidDVDException if the quantity is null or less than or equal to 0.
     */
    private boolean isQuantityValid(Integer quantity) {
        if (quantity == null) {
            throw new InvalidDVDException("No quantity was provided");
        }

        if (quantity <= 0) {
            throw new InvalidDVDException("The quantity can't be negative or 0");
        }

        return true;
    }

    /**
     * Finds a list of DVDs with a given title.
     *
     * @param title the ID of the DVD to find.
     * @return a list of DvdDTOs representing the found DVDs.
     */
    private List<DvdDTO> findDvdsByTitle(String title) {
        List<Dvd> dvds = dvdRepository.findDvdsByTitle(title);

        return dvds.stream()
                .map(dvdDTOMapper::convert)
                .toList();
    }

    /**
     * Retrieves all DVDs from the database.
     *
     * @return a list of DvdDTOs representing the found DVDs.
     */
    private List<DvdDTO> findAllDvds() {
        List<Dvd> dvds = dvdRepository.findAllDvds();

        return dvds.stream()
                .map(dvdDTOMapper::convert)
                .toList();
    }
}
