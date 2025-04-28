package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Предназначен для возврата информации о связи {@link
 * com.thedan17.salesnet.core.object.entity.AccGroupLink} пользователю при GET запросах.
 */
@Schema(description = "DTO для предоставления информации о связи между аккаунтом и группой.")
@Data
public class AccGroupLinkDto {
  @Schema(description = "ID объекта связи")
  private Long id;

  @Schema(description = "ID объекта аккаунта")
  private Long accountId;

  @Schema(description = "ID объекта группы")
  private Long groupId;

  @Schema(description = "Время, когда связь была создана")
  private LocalDateTime linkedAt;

  @Schema(description = "Код, который указал аккаунт (опционально) при входе в приватную группу. Если не был указан, то является пустой строкой")
  private String inviteCode;
}
