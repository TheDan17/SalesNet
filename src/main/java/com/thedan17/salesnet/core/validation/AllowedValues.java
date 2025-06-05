package com.thedan17.salesnet.core.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AllowedValuesValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedValues {
  String message() default "Значение не входит в допустимый список";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};

  String[] value(); // допустимые значения
}

