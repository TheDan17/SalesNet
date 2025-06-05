package com.thedan17.salesnet.core.object.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class GroupAccountsDto {
  private Long id;
  private String name;
  private String description;
  private List<AccountInfoDto> accounts = new ArrayList<>();
}
