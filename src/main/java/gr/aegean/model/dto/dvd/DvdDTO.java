package gr.aegean.model.dto.dvd;

import gr.aegean.model.dvd.DvdGenre;

import java.util.UUID;


public record DvdDTO(UUID id, String title, DvdGenre genre, Integer quantity) {}
