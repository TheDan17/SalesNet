package com.thedan17.salesnet.exception;

public class InvalidRequestBodyException extends RuntimeException {
  public InvalidRequestBodyException(String message) {
    super(message);
  }
}
