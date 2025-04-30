package com.thedan17.salesnet.util;

import java.util.List;

public class PasswordCheckUtil {
  public static final short REQUIRED_PASSWORD_LENGTH = 8;
  public static final List<String> KEY_ROWS = List.of(
          "qwertyuiop", "poiuytrewq",
          "asdfghjkl", "lkjhgfdsa",
          "zxcvbnm", "mnbvcxz");

  public static boolean containsDigit(String password) {
    return password != null && password.matches(".*\\d.*");
  }

  public static boolean containsLowerCase(String password) {
    return password != null && password.matches(".*[a-z].*");
  }

  public static boolean containsUpperCase(String password) {
    return password != null && password.matches(".*[A-Z].*");
  }

  public static boolean doesNotContainRepeatedChars(String password) {
    return password != null && !password.matches("(.)\\1{2,}");
  }

  /**
   * @return true, если пароль состоит исключительно из латинских A-Z/a-z, цифр 0-9 и символа _
   */
  public static boolean containsOnlyAllowedChars(String password) {
    return password != null && password.matches("^[A-Za-z0-9_]+$");
  }

  public static boolean haveEnoughLength(String password){
    return password.length() >= REQUIRED_PASSWORD_LENGTH;
  }

  /**
   * Ловит любые N-символьные фрагменты из строк клавиатуры.
   *
   * @return true — если есть подпоследовательность длины ≥ seqLength
   */
  public static boolean containsKeyboardSequence(String password, int seqLength) {
    if (password == null || password.length() < seqLength) return false;
    String lower = password.toLowerCase();

    for (String row : KEY_ROWS) {
      for (int i = 0; i + seqLength <= row.length(); i++) {
        String fragment = row.substring(i, i + seqLength);
        if (lower.contains(fragment)) {
          return true;
        }
      }
    }
    return false;
  }

  public static boolean containsNumericSequence(String password, int seqLength) {
    if (password == null || password.length() < seqLength) {
      return false;
    }

    int incRun = 1, decRun = 1;
    for (int i = 1; i < password.length(); i++) {
      char prev = password.charAt(i - 1);
      char curr = password.charAt(i);

      if (curr - prev == 1) {
        incRun++;
        decRun = 1;
      } else if (prev - curr == 1) {
        decRun++;
        incRun = 1;
      } else {
        incRun = 1;
        decRun = 1;
      }

      if (incRun >= seqLength || decRun >= seqLength) {
        return true;
      }
    }
    return false;
  }

  /** Запрещаем любые 4-символьные фрагменты. */
  public static boolean doesNotContainKeyboardSequence(String password) {
    return !containsKeyboardSequence(password, 3) &&
            !containsNumericSequence(password, 3);
  }
}
