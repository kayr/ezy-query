package io.github.kayr.ezyquery.parser;

public class EzyParseException extends RuntimeException {

  public EzyParseException(String message) {
    super(message);
  }

  public EzyParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
