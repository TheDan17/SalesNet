package com.thedan17.salesnet.exception;

public class ExceptionCommonLiterals {
  private static class FormatLiterals {
    static String accountNotExist = "Account ID=%s not exist";
  }
  public static String accountNotExist(Long id) {
    return String.format(FormatLiterals.accountNotExist, id);
  }
}
