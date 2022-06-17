package io.github.kayr.ezyquery.util;

public interface Function<T, R> {

  R apply(T t) throws Exception;
}
