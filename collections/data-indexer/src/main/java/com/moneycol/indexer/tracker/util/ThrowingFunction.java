package com.moneycol.indexer.tracker.util;

import java.util.function.Function;

// https://dzone.com/articles/how-to-handle-checked-exception-in-lambda-expressi
@FunctionalInterface
interface ThrowingFunction<T, R, E extends Throwable> {
    R apply(T t) throws E;

    static <T, R, E extends Throwable> Function<T, R> unchecked(ThrowingFunction<T, R, E> f) {
        return t -> {
            try {
                return f.apply(t);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
