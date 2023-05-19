package gr.aegean.model.dvd;

import org.springframework.hateoas.Link;

import java.util.List;
import java.util.UUID;


public record DvdDTO(UUID id, String title, DvdGenre genre, int quantity, List<Link> links) {}
