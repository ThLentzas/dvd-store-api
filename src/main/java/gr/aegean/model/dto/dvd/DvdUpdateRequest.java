package gr.aegean.model.dto.dvd;

import gr.aegean.model.dvd.DvdGenre;
import jakarta.validation.constraints.Positive;


public record DvdUpdateRequest(
        @Positive
        Integer quantity,
        DvdGenre genre) {
}
