package com.moneycol.collections.server.domain;


public class InvalidCollectionException extends RuntimeException {
    public InvalidCollectionException(String message) {
        super(message);
    }
}
