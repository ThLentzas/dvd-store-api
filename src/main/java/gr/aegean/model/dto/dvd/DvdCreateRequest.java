package gr.aegean.model.dto.dvd;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import gr.aegean.model.dvd.DvdGenre;


public record DvdCreateRequest(

        @NotBlank(message = "The title is required")
        @Size(max = 100, message = "Invalid title. Title must not exceed 100 characters")
        String title,
        @NotNull(message = "The genre is required")
        DvdGenre genre,
        @NotNull(message = "The quantity is required")
        @Positive(message = "The quantity must be a positive number")
        Integer quantity) {
}
