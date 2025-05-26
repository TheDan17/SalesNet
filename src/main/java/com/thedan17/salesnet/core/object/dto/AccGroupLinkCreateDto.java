package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccGroupLinkCreateDto {
  @Schema(description = "ID объекта аккаунта")
  private Long accountId;

  @Schema(description = "ID объекта группы")
  private Long groupId;

  @Schema(description = "Код, который указал аккаунт (опционально) при входе в приватную группу. Если не был указан, то является пустой строкой")
  private String inviteCode;
}