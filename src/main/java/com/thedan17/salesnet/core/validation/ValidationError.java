package com.thedan17.salesnet.core.validation;

import lombok.Data;
import lombok.NonNull;

@Data
public class ValidationError {
  @NonNull private String source;
  @NonNull private String message;
}
