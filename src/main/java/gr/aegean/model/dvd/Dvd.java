package gr.aegean.model.dvd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;

import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "dvds")
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

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }

        if(obj == null) {
            return false;
        }

        if(obj instanceof Dvd DvdObj) {
            return (title.equals(DvdObj.title)) && (genre == DvdObj.genre) && (quantity.equals(DvdObj.getQuantity()));
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, genre, quantity);
    }
}