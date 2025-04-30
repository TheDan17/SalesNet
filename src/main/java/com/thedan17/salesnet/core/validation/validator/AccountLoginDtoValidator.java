package com.thedan17.salesnet.core.validation.validator;

import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.validation.ValidationError;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AccountLoginDtoValidator {
  @SuppressWarnings("resource")
  private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  /** Валидация {@link AccountSignupDto} согласно правилам аннотации {@link Schema}. */
  public static List<ValidationError> validate(AccountSignupDto dto) {
    Set<ConstraintViolation<AccountSignupDto>> violations = validator.validate(dto);
    // Spring errors
    List<ValidationError> errors = new ArrayList<>(violations.stream()
            .map(violation -> new ValidationError(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
            )).toList());
    // Password errors
    errors.addAll(PasswordValidator.validate(dto.getPassword()));
    // Specific errors
    if (!dto.getType().equals("Physical") && !dto.getSecondName().isBlank()){
      errors.add(new ValidationError(AccountSignupDto.Fields.secondName,
              "Second name field should be empty, when type is not 'Physical'"));
    }
    return errors;
  }
}
