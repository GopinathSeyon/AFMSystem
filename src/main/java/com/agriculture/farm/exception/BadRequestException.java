package com.agriculture.farm.exception;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}
