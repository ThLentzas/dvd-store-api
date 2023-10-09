package gr.aegean.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import gr.aegean.model.dto.dvd.DvdDTO;
import gr.aegean.service.DvdService;
import gr.aegean.model.dto.dvd.DvdUpdateRequest;
import gr.aegean.model.dto.dvd.DvdCreateRequest;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/dvds")
public class DvdController {
    private final DvdService dvdService;

    @PostMapping
    public ResponseEntity<DvdDTO> createDvd(@Valid @RequestBody DvdCreateRequest createRequest,
                                            UriComponentsBuilder uriBuilder) {
        DvdDTO dvdDTO = dvdService.createDvd(createRequest);

        URI location = uriBuilder
                .path("/api/v1/dvds/{dvdId}")
                .buildAndExpand(dvdDTO.id())
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        return new ResponseEntity<>(dvdDTO, headers, HttpStatus.CREATED);
    }

    @GetMapping("{dvdId}")
    public ResponseEntity<DvdDTO> findDvdById(@PathVariable String dvdId) {
        DvdDTO dvdDTO = dvdService.findDvdById(dvdId);

        return new ResponseEntity<>(dvdDTO, HttpStatus.OK);
    }

    /*
        By default, it's true, meaning it will expect the title query param
     */
    @GetMapping
    public ResponseEntity<List<DvdDTO>> findDvds(@RequestParam(value = "title", required = false) String title) {
        List<DvdDTO> dvdsDTO = dvdService.findDvds(title);

        return new ResponseEntity<>(dvdsDTO, HttpStatus.OK);
    }

    @PutMapping("{dvdId}")
    public ResponseEntity<DvdDTO> updateDvd(@Valid @RequestBody DvdUpdateRequest updateRequest,
                                            @PathVariable String dvdId) {
        DvdDTO dvdDTO = dvdService.updateDvd(dvdId, updateRequest);

        return new ResponseEntity<>(dvdDTO, HttpStatus.OK);
    }

    @DeleteMapping("{dvdId}")
    public ResponseEntity<Void> deleteDvd(@PathVariable String dvdId) {
        dvdService.deleteDvd(dvdId);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

