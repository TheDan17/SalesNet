package com.thedan17.salesnet.core.validation;

import java.util.List;

public interface Validator<T> {
  List<ValidationError> validate(T object);
}
