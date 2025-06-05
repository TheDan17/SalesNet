package com.thedan17.salesnet.core.object.dto;

import com.thedan17.salesnet.core.validation.AllowedValues;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;

/** DTO для передачи регистрационной информации от пользователя к серверу. */
@Schema(description = "DTO для предоставления информации при регистрации аккаунта.")
@Data
@FieldNameConstants
@AllArgsConstructor
@NoArgsConstructor
public class AccountSignupDto {
  @Schema(description = "Уникальный буквенный идентификатор, желаемый пользователем")
  @NotBlank
  private String login;

  @Schema(description = "E-mail, на который будут приходить служебного рода письма")
  @Email
  @NotBlank
  private String email;

  @Schema(description = """
        Пароль, хэш которого будет использоваться для входа пользователя в будущий аккаунт.
        Должен соответствовать следующим требованиям:
        - Длина 8 или более символов
        - Только латинские символы, цифры и нижнее подчеркивание
        - Хотя бы одна цифра
        - Хотя бы одна прописная буква
        - Хотя бы одна строчная буква
        - Отсутствует последовательности, содержащие более трех символов типа 'qwerty'
        - Отсутствует последовательности, содержащие более трех символов типа '123456'
        """)
  @NotBlank
  private String password;

  @Schema(description = "Имя, если физическое лицо, иначе поле для названия ИП/компании")
  @NotBlank
  private String firstName;

  @Schema(description = "Фамилия, используется, только если это физическое лицо, иначе пустое поле")
  @NotNull
  private String secondName;

  @Schema(description = "Тип аккаунта, который указывает на принадлежность к физ.лицу/ИП/ООО/ОАО")
  @NotBlank
  @AllowedValues(value = {"Physical", "OOO", "OAO", "SoleProp", "Corporate", "Business"})
  private String type;
}
