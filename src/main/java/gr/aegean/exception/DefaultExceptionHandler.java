package gr.aegean.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.time.LocalDateTime;

@ControllerAdvice
public class DefaultExceptionHandler {

    @ExceptionHandler(InvalidDVDException.class)
    private ResponseEntity<ApiError> handleInvalidDVDException(InvalidDVDException ide,
                                                                 HttpServletRequest httpServletRequest) {
        ApiError apiError = new ApiError(httpServletRequest.getRequestURI(), ide.getMessage(),
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    private ResponseEntity<ApiError> handleDuplicateResourceException(DuplicateResourceException dre,
                                                                      HttpServletRequest httpServletRequest) {
        ApiError apiError = new ApiError(httpServletRequest.getRequestURI(), dre.getMessage(),
                HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DvdNotFoundException.class)
    private ResponseEntity<ApiError> handleDVDNotFoundException(DvdNotFoundException nfe,
                                                              HttpServletRequest httpServletRequest) {
        ApiError apiError = new ApiError(httpServletRequest.getRequestURI(), nfe.getMessage(),
                HttpStatus.NOT_FOUND.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    private ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException bce,
                                                                   HttpServletRequest httpServletRequest) {
        ApiError apiError = new ApiError(httpServletRequest.getRequestURI(), bce.getMessage(),
                HttpStatus.UNAUTHORIZED.value(), LocalDateTime.now());

        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }
}
