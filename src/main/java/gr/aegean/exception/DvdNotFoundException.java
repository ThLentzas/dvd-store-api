package gr.aegean.exception;

public class DvdNotFoundException extends RuntimeException{
    public DvdNotFoundException(String message) {
        super(message);
    }
}
