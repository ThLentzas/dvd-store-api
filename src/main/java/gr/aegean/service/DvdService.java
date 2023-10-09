package gr.aegean.service;

import gr.aegean.exception.DuplicateResourceException;
import gr.aegean.exception.ResourceNotFoundException;
import gr.aegean.entity.Dvd;
import gr.aegean.model.dto.dvd.DvdCreateRequest;
import gr.aegean.model.dto.dvd.DvdDTO;
import gr.aegean.mapper.DvdDTOMapper;
import gr.aegean.model.dto.dvd.DvdUpdateRequest;
import gr.aegean.repository.DvdRepository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;


/**
 * Service class for Dvd-related functionality and caching
 */
@Service
@RequiredArgsConstructor
public class DvdService {
    private final DvdRepository dvdRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DvdDTOMapper dvdDTOMapper = new DvdDTOMapper();
    private static final String CACHE_NAME = "dvds";

    /**
     * Creates a new DVD to the database and adds it to Redis cache.
     */
    public DvdDTO createDvd(DvdCreateRequest dvdCreateRequest) {
        Dvd dvd = new Dvd(
                dvdCreateRequest.title(),
                dvdCreateRequest.genre(),
                dvdCreateRequest.quantity()
        );

        validateDvd(dvd);

        if (dvdRepository.existsDvdWithTitle(dvd.getTitle())) {
            throw new DuplicateResourceException("Dvd already exists");
        }

        dvd = dvdRepository.createDvd(dvd);
        redisTemplate.opsForHash().put(CACHE_NAME, dvd.getId().toString(), dvd);

        return dvdDTOMapper.convert(dvd);
    }

    public DvdDTO updateDvd(String dvdId, DvdUpdateRequest updateRequest) {
        if (updateRequest.quantity() == null && updateRequest.genre() == null) {
            throw new IllegalArgumentException("Either quantity or genre must be provided");
        }

        Dvd dvd = dvdRepository.findDvdByID(dvdId)
                .orElseThrow(() -> new ResourceNotFoundException("Dvd was not found with id: " + dvdId));

        updateDvdProperties(dvd, updateRequest);
        redisTemplate.opsForHash().put(CACHE_NAME, dvdId, dvd);

        return dvdDTOMapper.convert(dvd);
    }

    /**
     * First checks the Redis cache for the DVD with the given ID. If it exists in the cache, returns a DvdDTO.
     * If not, retrieves the data from the database, saves it to the Redis cache and returns a DvdDTO containing
     * the retrieved data.
     */
    public DvdDTO findDvdById(String dvdId) {
        Dvd dvd = (Dvd) redisTemplate.opsForHash().get(CACHE_NAME, dvdId);
        if (dvd != null) {
            return dvdDTOMapper.convert(dvd);
        }

        dvd = dvdRepository.findDvdByID(dvdId)
                .orElseThrow(() -> new ResourceNotFoundException("Dvd was not found with id: " + dvdId));

        redisTemplate.opsForHash().put(CACHE_NAME, dvd.getId().toString(), dvd);

        return dvdDTOMapper.convert(dvd);
    }

    /**
     * Retrieves all DVDs from the database. If a non-null and non-empty title is provided,
     * Dvds matching the title are returned, otherwise all Dvds are returned.
     */
    public List<DvdDTO> findDvds(String title) {
        if (title != null && !title.isBlank()) {
            return findDvdsByTitle(title);
        }

        return findAllDvds();
    }

    public void deleteDvd(String dvdID) {
        dvdRepository.deleteDvd(dvdID);
        redisTemplate.opsForHash().delete(CACHE_NAME, dvdID);
    }

    private void validateDvd(Dvd dvd) {
        String sanitizedTitle = sanitizeTitle(dvd.getTitle());
        dvd.setTitle(sanitizedTitle);

        if(dvd.getTitle().isBlank()) {
            throw new IllegalArgumentException("No title was provided");
        }
    }

    /**
     * We replace any characters in the title that are not alphanumeric, spaces, periods, pluses, colons, apostrophes
     * or hyphens with an empty string. Any sequence of one or more spaces within the title is replaced with
     * a single space. Prevents storage of unrecognizable titles (e.g., "!#$&$@") and enables consistent handling of
     * titles irrespective of the spacing between words (e.g., "Harry Potter" and "Harry  Potter" are treated
     * as identical).
     */
    private String sanitizeTitle(String title) {
        title = title.trim();
        title = title.replaceAll("[^a-zA-Z0-9\\s+.':-]", "");
        title = title.replaceAll("\\s+", " ");

        return title;
    }

    private List<DvdDTO> findDvdsByTitle(String title) {
        List<Dvd> dvds = dvdRepository.findDvdsByTitle(title);

        return dvds.stream()
                .map(dvdDTOMapper::convert)
                .toList();
    }

    private List<DvdDTO> findAllDvds() {
        List<Dvd> dvds = dvdRepository.findAllDvds();

        return dvds.stream()
                .map(dvdDTOMapper::convert)
                .toList();
    }

    private void updateDvdProperties(Dvd dvd, DvdUpdateRequest updateRequest) {
        updateDvdPropertyIfNonNull(
                updateRequest.quantity(),
                dvd::setQuantity);

        updateDvdPropertyIfNonNull(
                updateRequest.genre(),
                dvd::setGenre);

        dvdRepository.updateDvd(dvd);
    }

    private <T> void updateDvdPropertyIfNonNull(T property, Consumer<T> updater) {
        if (property != null) {
            updater.accept(property);
        }
    }
}
