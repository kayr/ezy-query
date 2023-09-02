package io.github.kayr.ezyquery.api;

public class EzyQueryUnCaughtException extends RuntimeException {

  public EzyQueryUnCaughtException(String message, Throwable cause) {
    super(message, cause);
  }

  public interface ThrowingSupplier<T> {
    T get() throws Exception;
  }

  public static <T> T doGet(ThrowingSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (Exception e) {
      throw new EzyQueryUnCaughtException(e.getMessage(), e);
    }
  }
}
