package com.example.demo.exceptions;


public class CsvProcessingException extends RuntimeException {
    public CsvProcessingException(String message) {
        super(message);
    }
    
    public CsvProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}