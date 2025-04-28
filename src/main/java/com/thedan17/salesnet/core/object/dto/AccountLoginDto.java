package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/** DTO для передачи регистрационной информации от пользователя к серверу. */
@Schema(description = "DTO для предоставления информации при регистрации аккаунта.")
@Data
@FieldNameConstants
public class AccountLoginDto {
  @Schema(description = "Уникальный буквенный идентификатор, желаемый пользователем")
  private String login;
  @Schema(description = "E-mail, на который будут приходить служебного рода письма")
  private String email;
  @Schema(description = "Пароль, хэш которого будет использоваться для входа пользователя в будущий аккаунт")
  private String password;
  @Schema(description = "Имя, если физическое лицо, иначе поле для названия ИП/компании")
  private String firstName;
  @Schema(description = "Фамилия, используется, только если это физическое лицо, иначе пустое поле")
  private String secondName;
  @Schema(description = "Тип аккаунта, который указывает на принадлежность к физ.лицу/ИП/ООО/ОАО")
  private String type;
}
