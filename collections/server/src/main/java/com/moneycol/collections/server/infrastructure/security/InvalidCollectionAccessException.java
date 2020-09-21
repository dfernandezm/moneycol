package com.moneycol.collections.server.infrastructure.security;

public class InvalidCollectionAccessException extends RuntimeException {
    public InvalidCollectionAccessException(String message) {
        super(message);
    }
}
