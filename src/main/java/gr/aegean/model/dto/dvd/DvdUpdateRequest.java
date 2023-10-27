package gr.aegean.model.dto.dvd;

import jakarta.validation.constraints.Positive;

import gr.aegean.model.dvd.DvdGenre;


public record DvdUpdateRequest(
        @Positive(message = "The quantity must be a positive number")
        Integer quantity,
        DvdGenre genre) {
}
