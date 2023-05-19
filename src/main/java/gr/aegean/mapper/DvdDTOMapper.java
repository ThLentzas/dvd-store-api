package gr.aegean.mapper;

import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdDTO;
import org.springframework.core.convert.converter.Converter;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Service
public class DvdDTOMapper implements Converter<Dvd, DvdDTO> {

    /**
     * Creates a new DvdDTO instance using the provided DVD object and base URL.
     * Matches the DTO properties with data from the DVD object and includes links according to the HATEOAS approach.
     *
     * @param dvd the DVD object to create the DTO from.
     */
    @Override
    public DvdDTO convert(Dvd dvd) {
        return new DvdDTO(
                dvd.getId(),
                dvd.getTitle(),
                dvd.getGenre(),
                dvd.getQuantity(),
                List.of (Link.of(getBaseUrl() + "/dvds/" + dvd.getId().toString(), "self"),
                        Link.of(getBaseUrl() + "/dvds", "allDvds"))
        );
    }

    /**
     * Generates the base URL for the current context path and API version.
     * This method uses the ServletUriComponentsBuilder to build the base URL dynamically based on the current request.
     @return the base URL as a string, including the current context path and API version.
     */
    private String getBaseUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString() + "/api/v1";
    }
}
