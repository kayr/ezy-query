package io.github.kayr.ezyquery.util;

public interface ThrowingConsumer<T> {
  void accept(T t) throws Exception;
}
