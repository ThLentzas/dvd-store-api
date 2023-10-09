package gr.aegean.model.dto.dvd;

import gr.aegean.model.dvd.DvdGenre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record DvdCreateRequest(
        @NotBlank(message = "The title is required")
        String title,
        @NotNull(message = "The genre is required")
        DvdGenre genre,
        @NotNull(message = "The quantity is required")
        @Positive(message = "The quantity must be a positive number")
        Integer quantity) {
}
