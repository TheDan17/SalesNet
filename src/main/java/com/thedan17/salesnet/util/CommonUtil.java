package com.thedan17.salesnet.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.function.Supplier;

/** Класс для статических вспомогательных методов, предназначенных для общего использования. */
public class CommonUtil {
  /** Конструктор для предотвращения создания экземпляра класса. */
  private CommonUtil() {
    throw new IllegalStateException("Utility class!");
  }

  /**
   * Метод для преобразования исключения в {@code Optional<>}.
   *
   * <p>Действует следующим образом: выполняется лямбда, которая должна вернуть {@code T}, причем:
   *
   * <ul>
   *   <li>Если значение возвращается из функции, то возвращает {@code Optional.ofNullable()}
   *   <li>Если выбрасывается {@code exceptionType}, то возвращает {@code Optional.empty()}
   *   <li>Если выбрасывается любое другое исключение, то выбрасывает его заново
   * </ul>
   */
  public static <T, E extends Exception> Optional<T> optionalFromException(
      Supplier<T> supplier, Class<E> exceptionType) {
    try {
      return Optional.ofNullable(supplier.get());
    } catch (Exception e) {
      if (exceptionType.isInstance(e)) {
        return Optional.empty();
      }
      throw e;
    }
  }

  /** Временный метод для хеширования пароля по методу SHA-256. */
  public static String hashWithSha256(String data) {
    MessageDigest digest;
    try {
      digest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("No SHA-256 algorithm (with cause)", e);
    }
    byte[] hashBytes = digest.digest(data.getBytes());
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

}
