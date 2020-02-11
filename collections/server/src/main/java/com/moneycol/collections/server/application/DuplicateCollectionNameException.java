package com.moneycol.collections.server.application;

public class DuplicateCollectionNameException extends RuntimeException {
    public DuplicateCollectionNameException(String message) {
        super(message);
    }
}
