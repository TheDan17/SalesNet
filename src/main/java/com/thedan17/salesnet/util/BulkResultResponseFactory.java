package com.thedan17.salesnet.util;

import com.thedan17.salesnet.core.object.data.BulkOperationResult;
import com.thedan17.salesnet.core.object.data.BulkOperationResultDetailed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Утилита формирования ответов для различных {@code Bulk*Result}. */
public class BulkResultResponseFactory {
  /**
   * Метод формирования кода ответа в зависимости от количества успешных элементов bulk операции.
   */
  public static ResponseEntity<BulkOperationResultDetailed> fromResult(BulkOperationResultDetailed result) {
    if (result.getFailureAmount() > 0 && result.getSuccessAmount() > 0) {
      return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
    } else if (result.getSuccessAmount() == 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
    } else {
      return ResponseEntity.status(HttpStatus.OK).body(result);
    }
  }
}
