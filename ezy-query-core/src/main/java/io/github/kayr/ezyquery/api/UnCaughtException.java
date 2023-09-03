package io.github.kayr.ezyquery.api;

import io.github.kayr.ezyquery.util.ThrowingSupplier;

public class UnCaughtException extends RuntimeException {

    public UnCaughtException(Throwable cause) {
        super(cause);
    }

    public UnCaughtException(String message, Throwable cause) {
        super(message, cause);
    }

    public static <T> T doGet(ThrowingSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            throw new UnCaughtException(e.getMessage(), e);
        }
    }
}
