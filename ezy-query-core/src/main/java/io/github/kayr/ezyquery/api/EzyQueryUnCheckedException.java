package io.github.kayr.ezyquery.api;

public class EzyQueryUnCheckedException extends RuntimeException {

  public EzyQueryUnCheckedException(String message, Throwable cause) {
    super(message, cause);
  }

  public static void throwException(String message, Throwable cause) {
    throw new EzyQueryUnCheckedException(message, cause);
  }
}
