package com.thedan17.salesnet.core.object.data;

import java.time.Duration;
import java.time.Instant;
import lombok.Data;

@Data
public class AsyncTaskInfo<I, O> {
  private final Integer id;
  private volatile Status status;
  private final I params;
  private O result;
  private Instant completedAt;
  private Long minutesUntilDeletion;

  public AsyncTaskInfo(Integer id, I params) {
    this.id = id;
    this.params = params;
    this.status = Status.PENDING;
    this.result = null;
  }

  public void calcDuration() {
    Instant deletionTime = completedAt.plus(Duration.ofMinutes(5));
    Instant now = Instant.now();
    Duration time = Duration.between(now, deletionTime);
    this.minutesUntilDeletion = time.toMinutes();
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
    calcDuration();
  }

  public Long getMinutesUntilDeletion() {
    return this.minutesUntilDeletion;
  }

  public enum Status {
    PENDING,
    RUNNING,
    DONE,
    FAILED
  }
}