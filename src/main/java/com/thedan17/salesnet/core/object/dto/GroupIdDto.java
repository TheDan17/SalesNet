package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Класс-проекция сущности {@code Group} (без связей {@code AccGroupLink}).
 *
 * <p>Используется для GET запросов
 */
@Schema(
    description =
        "DTO для предоставления информации о группе, которая была получена непрямым способом (например, через поиск по критериям)")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupIdDto {
  @Schema(description = "ID группы, присваиваемое системой")
  private Long id;

  @Schema(description = "Неуникальное имя группы")
  private String name;

  @Schema(description = "Описание группы")
  private String description;

  @Schema(description = "Дата и время создания группы")
  private LocalDateTime createdAt;

  @Schema(
      description =
          "ID аккаунта, который владеет группой (может меняться по желанию текущего владельца)")
  private Long ownerId;
}
