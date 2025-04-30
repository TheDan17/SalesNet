package com.thedan17.salesnet.core.object.data;

import lombok.Data;

@Data
public class BulkResultShort {
  private Integer successElements;
  private Integer failureElements;

  public enum BulkStatus {
    ALL_SUCCESS,
    MULTI_STATUS,
    TOTAL_FAILURE
  }

  public BulkStatus getStatus() {
    if (successElements.equals(0)) {
      return BulkStatus.TOTAL_FAILURE;
    } else if (failureElements.equals(0)) {
      return BulkStatus.ALL_SUCCESS;
    } else {
      return BulkStatus.MULTI_STATUS;
    }
  }
}
