package com.example.backend.util.error;

public class EmailInvalidException extends Exception {
    public EmailInvalidException(String message) {
        super(message);
    }
}
