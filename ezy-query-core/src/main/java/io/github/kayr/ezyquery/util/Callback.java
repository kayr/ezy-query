package io.github.kayr.ezyquery.util;

public interface Callback<T> {

  void call(T t) throws Exception;
}
