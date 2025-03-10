package com.thedan17.salesnet.util;

import java.util.Optional;
import java.util.function.Supplier;

/** Класс для статических вспомогательных методов, предназначенных для общего использования. */
public class CommonUtil {
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
      throw new RuntimeException(e);
    }
  }
}
