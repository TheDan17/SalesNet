package com.thedan17.salesnet.core.object.data;

import com.thedan17.salesnet.core.validation.ValidationError;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.util.Pair;

/** Класс для представления результатов обработки элементов. */
@Getter
@Accessors(chain = true)
public class BulkResultDetailed {
  private Integer totalAmount = 0;
  private Integer successAmount = 0;
  private Integer failureAmount = 0;
  private final List<ElementResult> results = new ArrayList<>();

  /** Внутренний класс для представления результата обработки каждого элемента. */
  @Data
  @Accessors(chain = true)
  @RequiredArgsConstructor
  public static class ElementResult {
    @NonNull Long index;
    @NonNull ElementStatus status;
    @NonNull List<ElementError> errors;
  }

  /** Класс, идентичный {@link ValidationError}. */
  @Data
  @Accessors(chain = true)
  public static class ElementError {
    @NonNull String source;
    @NonNull String message;
  }

  /** Значение успешности обработки элемента. */
  public enum ElementStatus {
    SUCCESS,
    FAILURE
  }

  /** Простое конвертирование аналогичного класса во внутренний. */
  private static ElementError convertToBulkError(ValidationError error) {
    return new ElementError(error.getSource(), error.getMessage());
  }

  /** Присваивание количества элементов с помощью StreamAPI на основе имеющихся результатов. */
  private void initAmounts() {
    this.totalAmount = this.results.size();
    this.successAmount = Math.toIntExact(this.results.stream()
            .filter(elem -> elem.getStatus() == ElementStatus.SUCCESS).count());
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

  private void updateResult(ElementResult elem) {
    Optional<ElementResult> result = this.results.stream()
            .filter(item -> item.getIndex().equals(elem.getIndex()))
            .findFirst();
    if (result.isPresent()){
      if (elem.status == ElementStatus.FAILURE) {
        result.get().status = elem.status;
        result.get().errors.addAll(elem.errors);
      }
    } else {
      this.addResult(elem);
    }
  }

  /** Обёртка для добавления результата, с удобной активацией подсчёта элементов.
   *
   * <p>Если объект с таким индексом уже существует, обновляет его.
   * @see BulkResultDetailed#updateResult(ElementResult)
   */
  private void addResult(ElementResult result, boolean isUpdateAmounts) {
    Optional<ElementResult> elem = this.results.stream()
            .filter(item -> item.getIndex().equals(result.getIndex()))
            .findFirst();
    if (elem.isPresent()) {
      updateResult(result);
    } else {
      this.results.add(result);
      if (isUpdateAmounts) {
        increaseAmounts(result.status == ElementStatus.SUCCESS);
      }
    }
  }

  /**
   * Перегруженный метод для значения по умолчанию, реализует неявный вызов {@code increaseAmounts}.
   *
   * @see BulkResultDetailed#addResult(ElementResult, boolean)
   */
  public void addResult(ElementResult elementResult) {
    this.addResult(elementResult, true);
  }

  /** Инъекция зависимости для автоматизированной обработки одного элемента. */
  public static <T> ElementResult createResult(
      long index, T item, Function<T, List<ValidationError>> processor) {
    List<ElementError> processorResult = new ArrayList<>(
        processor.apply(item).stream()
            .map(BulkResultDetailed::convertToBulkError)
            .toList());
    ElementResult result =
        new ElementResult(-1L, ElementStatus.SUCCESS, new ArrayList<>());
    if (!processorResult.isEmpty()) {
      result.status = ElementStatus.FAILURE;
      result.errors = processorResult;
    } else {
      result.status = ElementStatus.SUCCESS;
    }
    result.index = index;
    return result;
  }

  /**
   * Обёртка для прямого добавления результата {@link BulkResultDetailed#createResult}.
   *
   * <p>При добавлении результат передаётся в {@link #addResult(ElementResult, boolean)}
   * с флагом {@code isUpdateAmounts = true}.
   */
  public <T> void addResult(long index, T item, Function<T, List<ValidationError>> processor) {
    this.addResult(createResult(index, item, processor), true);
  }

  /** Аналог {@link BulkResultDetailed#addResult(long, Object, Function)}, но для списка целиком. */
  public <T> void addResults(List<Pair<Long, T>> indexItems, Function<T, List<ValidationError>> processor) {
    indexItems.forEach(pair ->
        addResult(
                createResult(
                        pair.getFirst(),
                        pair.getSecond(),
                        processor
                ),
                false
        )
    );
    initAmounts();
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
