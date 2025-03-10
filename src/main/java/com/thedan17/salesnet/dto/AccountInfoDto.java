package com.thedan17.salesnet.dto;

import java.time.LocalDateTime;
import lombok.Data;

/** DTO для получения публичной информации об аккаунте. */
@Data
public class AccountInfoDto {
  private Long id;
  public String fullName;
  private String type;
  private LocalDateTime createdAt;
}
