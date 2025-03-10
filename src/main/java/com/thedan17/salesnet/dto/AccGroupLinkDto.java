package com.thedan17.salesnet.dto;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * Предназначен для возврата информации о связи {@code AccGroupLink} пользователю при GET запросах.
 */
@Data
public class AccGroupLinkDto {
  private Long id;
  private Long accountId;
  private Long groupId;
  private LocalDateTime linkedAt;
  private String inviteCode;
}
