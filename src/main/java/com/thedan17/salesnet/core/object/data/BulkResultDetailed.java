package com.thedan17.salesnet.core.object.data;

import com.thedan17.salesnet.core.validation.ValidationError;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import lombok.*;
import lombok.experimental.Accessors;

/** Класс для представления результатов обработки элементов. */
@Getter
@Accessors(chain = true)
public class BulkResultDetailed {
  private Integer totalAmount = 0;
  private Integer successAmount = 0;
  private Integer failureAmount = 0;
  private final List<BulkElementResult> results = new ArrayList<>();

  /** Внутренний класс для представления результата обработки каждого элемента. */
  @Data
  @Accessors(chain = true)
  @NoArgsConstructor
  @RequiredArgsConstructor
  public static class BulkElementResult {
    @NonNull Long index;
    @NonNull BulkElementStatus status;
    @NonNull List<BulkElementError> errors;
  }

  /** Класс, идентичный {@link ValidationError}. */
  @Data
  @Accessors(chain = true)
  public static class BulkElementError {
    @NonNull String source;
    @NonNull String message;
  }

  /** Значение успешности обработки элемента. */
  public enum BulkElementStatus {
    SUCCESS,
    FAILURE
  }

  /** Простое конвертирование аналогичного класса во внутренний. */
  private static BulkElementError convertToBulkError(ValidationError error) {
    return new BulkElementError(error.getSource(), error.getMessage());
  }

  /** Присваивание количества элементов с помощью StreamAPI на основе имеющихся результатов. */
  private void initAmounts() {
    this.totalAmount = this.results.size();
    this.successAmount = Math.toIntExact(this.results.stream()
            .filter(elem -> elem.getStatus() == BulkElementStatus.SUCCESS).count());
    this.failureAmount = totalAmount - successAmount;
  }

  /** Увеличение характеристик кол-ва элементов при итеративном подходе. */
  private void increaseAmounts(boolean isSuccess) {
    if (Boolean.TRUE.equals(isSuccess)) {
      this.successAmount++;
    } else {
      this.failureAmount++;
    }
    this.totalAmount++;
  }

  /** Обёртка для добавления результата, с удобной активацией подсчёта элементов. */
  private void addResult(BulkElementResult result, boolean isUpdateAmounts) {
    this.results.add(result);
    if (isUpdateAmounts) {
      increaseAmounts(result.status == BulkElementStatus.SUCCESS);
    }
  }

  /**
   * Перегруженный метод для значения по умолчанию, реализует неявный вызов {@code increaseAmounts}.
   *
   * @see BulkResultDetailed#addResult(BulkElementResult, boolean)
   */
  public void addResult(BulkElementResult bulkElementResult) {
    this.addResult(bulkElementResult, true);
  }

  /** Инъекция зависимости для автоматизированной обработки одного элемента. */
  public static <T> BulkElementResult createResult(
      long index, T item, Function<T, List<ValidationError>> processor) {
    List<BulkElementError> processorResult =
        processor.apply(item).stream()
            .map(BulkResultDetailed::convertToBulkError)
            .toList();
    BulkElementResult result = new BulkElementResult();
    if (!processorResult.isEmpty()) {
      result.status = BulkElementStatus.FAILURE;
      result.errors = processorResult;
    } else {
      result.status = BulkElementStatus.SUCCESS;
    }
    result.index = index;
    return result;
  }

  /**
   * Обёртка для прямого добавления результата {@link BulkResultDetailed#createResult}.
   *
   * <p>При добавлении результат передаётся в {@link #addResult(BulkElementResult, boolean)}
   * с флагом {@code isUpdateAmounts = true}.
   */
  public <T> void addResult(long index, T item, Function<T, List<ValidationError>> processor) {
    this.addResult(createResult(index, item, processor), true);
  }

  /** Аналог {@link BulkResultDetailed#addResult(long, Object, Function)}, но для списка целиком. */
  public <T> void addResults(List<T> items, Function<T, List<ValidationError>> processor) {
    IntStream.range(0, items.size())
            .forEach(i -> addResult(createResult(i, items.get(i), processor), false));
    initAmounts();
  }

  public void updateResult(BulkElementResult elem) {
    var result = this.results.stream().findFirst();
    if (result.isPresent()) {
      if (elem.status == BulkElementStatus.FAILURE){
        result.get().status = elem.status;
        if (result.get().errors == null) {
          result.get().errors = elem.errors;
        } else {
          result.get().errors.addAll(elem.errors);
        }
      }
    } else {
      this.addResult(elem);
    }
  }

  /** Получение процента успешных элементов на основе уже имеющихся значений. */
  public Integer getSuccessPercentage() {
    return totalAmount == 0 ? 0 : (successAmount * 100) / totalAmount;
  }

  /** Получение процента неудачных элементов на основе уже имеющихся значений. */
  public Integer getFailurePercentage() {
    return totalAmount == 0 ? 0 : (failureAmount * 100) / totalAmount;
  }
}
