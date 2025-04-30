package com.thedan17.salesnet.core.object.data;

import lombok.Data;

@Data
public class BulkResultShort {
  private BulkResultShort.BulkStatus status;
  private Integer successElements;
  private Integer failureElements;

  public enum BulkStatus {
    ALL_SUCCESS,
    MULTI_STATUS,
    TOTAL_FAILURE
  }

  public void makeStatus() {
    if (successElements.equals(0)) {
      this.status = BulkStatus.TOTAL_FAILURE;
    } else if (failureElements.equals(0)) {
      this.status = BulkStatus.ALL_SUCCESS;
    } else {
      this.status = BulkStatus.MULTI_STATUS;
    }
  }
}
