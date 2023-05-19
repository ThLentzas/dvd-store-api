package gr.aegean.controller;

import gr.aegean.model.dvd.Dvd;
import gr.aegean.model.dvd.DvdDTO;
import gr.aegean.service.DvdService;

import jakarta.websocket.server.PathParam;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;


/**
 * Controller that handles DVD-related requests.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dvds")
public class DvdController {
    private final DvdService dvdService;

    /**
     * Inserts a new DVD. Only users with the "ROLE_EMPLOYEE" authority are authorized
     * to make this request.
     *
     * @param dvd the DVD object to be inserted.
     * @param uriBuilder a builder for creating URIs.
     * @return a ResponseEntity with the created status code and location header.
     */
    @PostMapping
    public ResponseEntity<DvdDTO> createDvd(@RequestBody Dvd dvd, UriComponentsBuilder uriBuilder) {
        DvdDTO dvdDTO = dvdService.createDvd(dvd);

        URI location = uriBuilder
                .path("/api/v1/dvds/{dvdID}")
                .buildAndExpand(dvdDTO.id())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(dvdDTO, headers, HttpStatus.CREATED);
    }

    /**
     * Finds a Dvd resource by its ID. Only users with the "ROLE_EMPLOYEE" authority are authorized
     * to make this request.
     *
     * @param dvdID the ID of the Dvd to find.
     * @return a ResponseEntity containing the DvdDTO resource.
     */
    @GetMapping("{dvdID}")
    public ResponseEntity<DvdDTO> findDvdByID(@PathVariable String dvdID) {
        DvdDTO dvdDTO = dvdService.findDvdByID(dvdID);

        return new ResponseEntity<>(dvdDTO, HttpStatus.OK);
    }

    /**
     * Finds all DVDs. Only users with the "ROLE_EMPLOYEE" authority are authorized to make this request.
     *
     * @return a ResponseEntity containing a list of all DvdDTOs.
     */
    @GetMapping
    public ResponseEntity<List<DvdDTO>> findDvds(@PathParam("title") String title) {
        List<DvdDTO> dvdsDTO = dvdService.findDvds(title);

        return new ResponseEntity<>(dvdsDTO, HttpStatus.OK);
    }

    /**
     * Updates the details of a specific DVD.
     *
     * @param dvd   the updated details of the DVD
     * @param dvdID the ID of the DVD to update
     * @return a ResponseEntity indicating whether the update was successful or not
     */
    @PutMapping("{dvdID}")
    public ResponseEntity<DvdDTO> updateDvd(@RequestBody Dvd dvd, @PathVariable String dvdID) {
        DvdDTO dvdDTO = dvdService.updateDvd(dvdID, dvd.getQuantity(), dvd.getGenre());

        return new ResponseEntity<>(dvdDTO, HttpStatus.OK);
    }

    /**
     * Deletes a specific DVD from the system.
     *
     * @param dvdID the ID of the DVD to delete.
     * @return a ResponseEntity indicating whether the deletion was successful or not.
     */
    @DeleteMapping("{dvdID}")
    public ResponseEntity<Void> deleteDvd(@PathVariable String dvdID) {
        dvdService.deleteDvd(dvdID);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

