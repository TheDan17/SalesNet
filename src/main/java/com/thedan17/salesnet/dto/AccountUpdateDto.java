package com.thedan17.salesnet.dto;

import lombok.Data;

/** DTO для обновления разрешённых полей сущности {@code Account}. */
@Data
public class AccountUpdateDto {
  String firstName;
  String secondName;
  String type;
}
