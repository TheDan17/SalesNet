package com.thedan17.salesnet.core.object.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

/** Класс-проекция сущности {@code Group} без id и createdAt.
 *
 * <p>Используется для POST, UPDATE запросов
 */
@Schema(description = "DTO с информацией для создания группы")
@Data
public class GroupCreateDto {
  @Schema(description = "Имя будущей группы, может быть неуникальным")
  private String name;
  @Schema(description = "Описание будущей группы")
  private String description;
  @Schema(description = "ID аккаунта владельца")
  private Long ownerId;
}