package com.thedan17.salesnet.util;

import java.util.List;
import java.util.regex.Pattern;

public class PasswordCheckUtil {
  public static final short REQUIRED_PASSWORD_LENGTH = 8;
  public static final short REQUIRED_SEQUENCE_LENGTH_DETECT = 3;
  private static final Pattern UPPERCASE = Pattern.compile("[A-Z]");
  private static final Pattern LOWERCASE = Pattern.compile("[a-z]");
  private static final Pattern DIGIT = Pattern.compile("\\d");
  public static final List<String> KEY_ROWS =
      List.of(
          "qwertyuiop", "poiuytrewq",
          "asdfghjkl", "lkjhgfdsa",
          "zxcvbnm", "mnbvcxz");

  /** Переопределение дефолтного конструктора приватным для утилитного класса. */
  private PasswordCheckUtil() {
    // for preventing bad usage
  }

  public static boolean containsUpperCase(String password) {
    return password != null && UPPERCASE.matcher(password).find();
  }

  public static boolean containsLowerCase(String password) {
    return password != null && LOWERCASE.matcher(password).find();
  }

  public static boolean containsDigit(String password) {
    return password != null && DIGIT.matcher(password).find();
  }

  public static boolean doesNotContainRepeatedChars(String password) {
    if (password == null) {
      return true;
    }
    return !Pattern.compile("(.)\\1{2,}").matcher(password).find();
  }

  /**
   * @return true, если пароль состоит исключительно из латинских A-Z/a-z, цифр 0-9 и символа _
   */
  public static boolean containsOnlyAllowedChars(String password) {
    return password != null && password.matches("^\\w+$");
  }

  public static boolean haveEnoughLength(String password) {
    return password != null && password.length() >= REQUIRED_PASSWORD_LENGTH;
  }

  /**
   * Ловит любые N-символьные фрагменты из строк клавиатуры.
   *
   * @return true — если есть подпоследовательность длины ≥ seqLength
   */
  public static boolean containsKeyboardSequence(String password, int seqLength) {
    if (password == null || password.length() < seqLength) {
      return false;
    }
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
    password = password.toLowerCase();

    int incRun = 1;
    int decRun = 1;
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

  /** Запрещаем любые символьные фрагменты заданной длины. */
  public static boolean doesNotContainSequences(String password) {
    return !containsKeyboardSequence(password, REQUIRED_SEQUENCE_LENGTH_DETECT)
        && !containsNumericSequence(password, REQUIRED_SEQUENCE_LENGTH_DETECT);
  }
}
