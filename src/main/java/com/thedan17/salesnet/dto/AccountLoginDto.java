package com.thedan17.salesnet.dto;

import lombok.Data;

/** DTO для передачи регистрационной информации от пользователя к серверу. */
@Data
public class AccountLoginDto {
  private String login;
  private String email;
  private String password;
  private String firstName;
  private String secondName;
  private String type;
}
