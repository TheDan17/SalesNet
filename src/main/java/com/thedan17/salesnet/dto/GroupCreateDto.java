package com.thedan17.salesnet.dto;

import lombok.Data;

/** Класс-проекция сущности {@code Group} без id и createdAt.
 *
 * <p>Используется для POST, UPDATE запросов
 */
@Data
public class GroupCreateDto {
  private String name;
  private String description;
}