package dev.devwrapped.backend.ai;

public class AiClassificationException extends RuntimeException {
    public AiClassificationException(String message) {
        super(message);
    }

    public AiClassificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
