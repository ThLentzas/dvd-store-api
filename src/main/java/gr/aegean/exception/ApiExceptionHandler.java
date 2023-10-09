package gr.aegean.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;


@ControllerAdvice
public class ApiExceptionHandler {

    /*
        Thrown by the @Valid annotation
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ma) {
        String errorMessage = ma.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ApiError apiError = new ApiError(
                errorMessage,
                HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<ApiError> handleIllegalArgumentException(IllegalArgumentException iae) {
        ApiError apiError = new ApiError(
                iae.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    private ResponseEntity<ApiError> handleDuplicateResourceException(DuplicateResourceException dre) {
        ApiError apiError = new ApiError(
                dre.getMessage(),
                HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    private ResponseEntity<ApiError> handleDVDNotFoundException(ResourceNotFoundException nfe) {
        ApiError apiError = new ApiError(
                nfe.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    private ResponseEntity<ApiError> handleBadCredentialsException(BadCredentialsException bce) {
        ApiError apiError = new ApiError(
                bce.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    private ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException ue) {
        ApiError apiError = new ApiError(
                ue.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );

        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ServerErrorException.class)
    private ResponseEntity<ApiError> handleServerErrorException(ServerErrorException se) {
        ApiError apiError = new ApiError(
                se.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value());

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
