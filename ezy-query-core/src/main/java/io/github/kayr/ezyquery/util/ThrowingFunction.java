package io.github.kayr.ezyquery.util;

public interface ThrowingFunction<T, R> {
  R apply(T t) throws Exception;
}
