package com.thedan17.salesnet.dto;

import java.time.LocalDateTime;
import lombok.Data;

/** Класс-проекция сущности {@code Group} без id.
 *
 * <p>Используется для POST, UPDATE запросов
 */
@Data
public class GroupDto {
  private String name;
  private String description;
  private LocalDateTime createdAt;
}
