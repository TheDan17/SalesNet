package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/** DTO для получения публичной информации об аккаунте. */
@Schema(description = "DTO для предоставления публичной информации о аккаунте для всех желающих")
@Data
public class AccountInfoDto {
  @Schema(description = "ID аккаунта, присваиваемый системой")
  private Long id;

  @Schema(
      description = "Комбинация имени (или названия компании) и фамилии (пустая, если компания)")
  private String fullName;

  @Schema(
      description =
          "Тип, указываемый при регистрации и указывающий на принадлежность к физ.лицу/ИП/ООО/ОАО")
  private String type;

  @Schema(description = "Время, когда аккаунт был создан (зарегистрирован)")
  private LocalDateTime createdAt;
}
