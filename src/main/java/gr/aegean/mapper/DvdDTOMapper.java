package gr.aegean.mapper;

import gr.aegean.entity.Dvd;
import gr.aegean.model.dto.dvd.DvdDTO;

import org.springframework.core.convert.converter.Converter;


public class DvdDTOMapper implements Converter<Dvd, DvdDTO> {
    @Override
    public DvdDTO convert(Dvd dvd) {
        return new DvdDTO(
                dvd.getId(),
                dvd.getTitle(),
                dvd.getGenre(),
                dvd.getQuantity()
        );
    }
}
