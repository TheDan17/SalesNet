package com.thedan17.salesnet.core.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;

public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, String> {

  private Set<String> allowedValues;

  @Override
  public void initialize(AllowedValues constraintAnnotation) {
    allowedValues = Set.of(constraintAnnotation.value());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || allowedValues.contains(value);
  }
}
