package io.github.kayr.ezyquery.util;

public interface ThrowingSupplier<T> {
  T get() throws Exception;
}
