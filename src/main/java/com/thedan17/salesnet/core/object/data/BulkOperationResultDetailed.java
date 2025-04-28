package com.thedan17.salesnet.core.object.data;

import java.util.List;
import java.util.function.Function;

import lombok.Data;

@Data
public class BulkOperationResultDetailed {
  Long successAmount;
  Long failureAmount;
  List<BulkElementResult> results;

  @Data
  public static class BulkElementResult {
    Long index;
    BulkElementStatus status;
    List<BulkElementError> errors;
  }

  @Data
  public static class BulkElementError {
    String source;
    String message;
  }

  public enum BulkElementStatus {
    SUCCESS,
    FAILURE
  }

  public void initAmounts() {
    this.successAmount = this.results.stream()
            .filter(elem -> elem.getStatus() == BulkElementStatus.SUCCESS)
            .count();
    this.failureAmount = this.results.size() - this.successAmount;
  }
  public void addResult(BulkElementResult bulkElementResult, boolean isInitAmounts) {
    this.results.add(bulkElementResult);
    if (isInitAmounts) {
      this.initAmounts();
    }
  }
  public void addResult(BulkElementResult bulkElementResult) {
    this.addResult(bulkElementResult, true);
  }

  public static <itemTypeT> BulkElementResult createResult(long index, itemTypeT item, Function<itemTypeT, List<BulkElementError>> processor) {
    List<BulkElementError> processorResult = processor.apply(item);
    BulkElementResult result = new BulkElementResult();
    result.index = index;
    if (!processorResult.isEmpty()) {
      result.status = BulkElementStatus.FAILURE;
      result.errors = processorResult;
    } else {
      result.status = BulkElementStatus.SUCCESS;
    }
    return result;
  }
}