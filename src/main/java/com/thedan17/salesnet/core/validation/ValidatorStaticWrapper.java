package com.thedan17.salesnet.core.validation;

import java.util.List;
import java.util.function.Function;

public class ValidatorStaticWrapper<T> implements Validator<T> {
  private final Function<T, List<ValidationError>> function;

  public ValidatorStaticWrapper(Function<T, List<ValidationError>> function) {
    this.function = function;
  }

  @Override
  public List<ValidationError> validate(T object) {
    return function.apply(object);
  }
}
