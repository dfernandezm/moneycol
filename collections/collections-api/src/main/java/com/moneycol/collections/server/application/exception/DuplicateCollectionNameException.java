package com.moneycol.collections.server.application.exception;

public class DuplicateCollectionNameException extends RuntimeException {
    public DuplicateCollectionNameException(String message) {
        super(message);
    }
}
