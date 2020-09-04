package net.kfoundation.java.serialization;

public class DeserializationError extends RuntimeException {
    public DeserializationError(String message) {
        super(message);
    }
    public DeserializationError(String message, Throwable cause) {
        super(message, cause);
    }
}
