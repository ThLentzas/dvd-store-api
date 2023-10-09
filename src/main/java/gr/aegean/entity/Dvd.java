package gr.aegean.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

import gr.aegean.model.dvd.DvdGenre;


@Setter
@Getter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Dvd {
    private UUID id;
    private String title;
    private DvdGenre genre;
    private Integer quantity;

    public Dvd(String title, DvdGenre genre, Integer quantity) {
        this.title = title;
        this.genre = genre;
        this.quantity = quantity;
    }
}