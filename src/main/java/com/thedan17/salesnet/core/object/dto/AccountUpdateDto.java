package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** DTO для обновления разрешённых полей сущности {@code Account}. */
@Schema(description = "DTO для обновления определенной информации аккаунта его владельцем. Все поля повторяют аналогичные у AccountSignupDto")
@Data
public class AccountUpdateDto {
  String firstName;
  String secondName;
  String type;
}
