package com.thedan17.salesnet.dto;

import java.time.LocalDateTime;
import lombok.Data;

/** Класс-проекция сущности {@code Group} (без связей {@code AccGroupLink}).
 *
 * <p>Используется для GET запросов
 */
@Data
public class GroupIdDto {
  private Long id;
  private String name;
  private String description;
  private LocalDateTime createdAt;
}
