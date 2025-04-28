package com.thedan17.salesnet.core.object.data;

import java.util.List;
import java.util.Set;
import lombok.Data;

@Data
public class BulkOperationResult {
  Integer successElements;
  Set<Integer> successIndexes;
  Integer failureElements;
  List<BulkProcessError> failureErrors;

  @Data
  public static class BulkProcessError {
    Integer elementIndex;
    String errorSource;
    String errorMessage;
  }
}
