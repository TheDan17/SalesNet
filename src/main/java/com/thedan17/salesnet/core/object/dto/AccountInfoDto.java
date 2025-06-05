package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** DTO для получения публичной информации об аккаунте. */
@Schema(description = "DTO для предоставления публичной информации о аккаунте для всех желающих")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoDto {
  @Schema(description = "ID аккаунта, присваиваемый системой")
  private Long id;

  @Schema(
      description = "Комбинация имени (или названия компании) и фамилии (пустая, если компания)")
  private String fullName;

  private String firstName;
  private String secondName;

  private String login;
  private String email;

  @Schema(
      description =
          "Тип, указываемый при регистрации и указывающий на принадлежность к физ.лицу/ИП/ООО/ОАО")
  private String type;

  @Schema(description = "Время, когда аккаунт был создан (зарегистрирован)")
  private LocalDateTime createdAt;
}
