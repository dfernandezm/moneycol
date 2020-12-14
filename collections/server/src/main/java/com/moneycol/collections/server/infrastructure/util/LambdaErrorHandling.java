package com.moneycol.collections.server.infrastructure.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class LambdaErrorHandling {

    /**
     * Wraps the given {@link ThrowingSupplier} (which is a regular {@link Supplier} that throws a single
     * checked Exception) so that the checked exception is properly logged and wrapped in a RuntimeException
     *
     * See: https://www.baeldung.com/java-lambda-exceptions
     *
     * @param supplier the original supplier, that needs wrapping around potential checked exceptions being thrown
     * @param <T> the Type being returned by the original Supplier
     * @param <E> the Type of the Throwable/Exception being handled
     * @return a Supplier, that wraps the original operation providing checked exception loggin/rethrowing
     */
    public static <T,E extends Throwable> Supplier<T> wrapInThrowingSupplier(ThrowingSupplier<T,E> supplier) {
        return () -> {
            try {
                return supplier.get();
            } catch (Throwable ex) {
                log.error("Exception occurred: ", ex);
                throw new RuntimeException(ex);
            }
        };
    }

    /**
     * Supporting functional interface that augments a regular Java 8 Supplier throwing a checked exception
     * @param <T>
     * @param <E>
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Throwable> {
        T get() throws E;
    }
}
