package com.thedan17.salesnet.core.validation.validator;

import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.validation.ValidationError;
import com.thedan17.salesnet.util.PasswordCheckUtil;
import java.util.ArrayList;
import java.util.List;

public class PasswordValidator {

  /** Переопределение дефолтного конструктора приватным для утилитного класса. */
  private PasswordValidator() {
    // for preventing bad usage
  }

  public static List<ValidationError> validate(String password) {
    List<ValidationError> errors = new ArrayList<>();
    if (!PasswordCheckUtil.haveEnoughLength(password)) {
      errors.add(new ValidationError(
              AccountSignupDto.Fields.password,
              String.format(
                      "Password length should be not less than %s",
                      PasswordCheckUtil.REQUIRED_PASSWORD_LENGTH
              )
      ));
    }
    if (!PasswordCheckUtil.containsOnlyAllowedChars(password)) {
      errors.add(new ValidationError(
              AccountSignupDto.Fields.password,
              "Password may contain only latin characters, numbers, and underscores"
      ));
    }
    if (!PasswordCheckUtil.containsDigit(password)) {
      errors.add(new ValidationError(
              AccountSignupDto.Fields.password,
              "Password should contain at least one digit"
      ));
    }
    if (!PasswordCheckUtil.containsLowerCase(password)) {
      errors.add(new ValidationError(
              AccountSignupDto.Fields.password,
              "Password should contain at least one lowercase letter"
      ));
    }
    if (!PasswordCheckUtil.containsUpperCase(password)) {
      errors.add(new ValidationError(
              AccountSignupDto.Fields.password,
              "Password should contain at least one uppercase letter"
      ));
    }
    if (!PasswordCheckUtil.doesNotContainSequences(password)) {
      errors.add(new ValidationError(
              AccountSignupDto.Fields.password,
              "Password should not contain sequences like 'qwerty'"
      ));
    }
    if (!PasswordCheckUtil.doesNotContainRepeatedChars(password)) {
      errors.add(new ValidationError(
              AccountSignupDto.Fields.password,
              "Password should not contain sequences like '12345'"
      ));
    }
    return errors;
  }
}
