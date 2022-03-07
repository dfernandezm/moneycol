package com.moneycol.collections.server.domain.base;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.UUID;

@Getter
@Accessors(fluent = true)
public abstract class Id<T> {
    private T id;

    protected Id(T id) {
        this.id = id;
    }

    public static String randomId() {
        return UUID.randomUUID().toString();
    }
}
