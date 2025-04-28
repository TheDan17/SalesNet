package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * Класс-проекция сущности {@code Group} без id.
 *
 * <p>Используется для POST, UPDATE запросов
 */
@Schema(
    description =
        "DTO для предоставления информации о группе, если запрашивалась по конкретному ID")
@Data
public class GroupDto {
  @Schema(description = "Имя группы")
  private String name;

  @Schema(description = "Описание группы")
  private String description;

  @Schema(description = "Дата создания группы")
  private LocalDateTime createdAt;
}
