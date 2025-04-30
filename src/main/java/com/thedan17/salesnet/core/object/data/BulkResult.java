package com.thedan17.salesnet.core.object.data;

import java.util.List;
import lombok.Data;

@Data
public class BulkResult {
  private Integer successElements;
  private Integer failureElements;
  private List<BulkProcessError> failureErrors;

  @Data
  public static class BulkProcessError {
    Integer elementIndex;
    String errorSource;
    String errorMessage;
  }
}
