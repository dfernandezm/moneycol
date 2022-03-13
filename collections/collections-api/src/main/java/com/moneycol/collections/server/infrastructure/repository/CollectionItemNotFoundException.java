package com.moneycol.collections.server.infrastructure.repository;


public class CollectionItemNotFoundException extends RuntimeException {
    public CollectionItemNotFoundException(String message) {
        super(message);
    }
}
