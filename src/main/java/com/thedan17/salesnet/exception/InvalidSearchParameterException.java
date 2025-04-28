package com.thedan17.salesnet.exception;

/** Исключение, указывающее на невалидные параметры поиска в запросе.
 *
 * <p>Является специфическим подмножеством статуса {@code 400 (Bad Request)}
 * */
public class InvalidSearchParameterException extends RuntimeException {
  /** Конструктор исключения. */
  public InvalidSearchParameterException(String message) {
    super(message);
  }
}
