package com.thedan17.salesnet.util;

import com.thedan17.salesnet.core.object.dto.AccGroupLinkDto;
import com.thedan17.salesnet.core.object.dto.AccountInfoDto;
import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.object.dto.AccountUpdateDto;
import com.thedan17.salesnet.core.object.dto.GroupCreateDto;
import com.thedan17.salesnet.core.object.dto.GroupDto;
import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.object.entity.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Класс для преобразования всех объектов {@code SalesNet}. */
@Mapper(componentModel = "spring")
public interface EntityMapper {

  /** Перевод из AccountLogin в Account для POST запроса. */
  Account loginDtoToAccount(AccountSignupDto accountSignupDto);

  /** Перевод из Account в AccountInfo для GET запроса. */
  @Mapping(
      target = "fullName",
      expression = "java(combineNames(account.getFirstName(), account.getSecondName()))")
  AccountInfoDto accountToInfoDto(Account account);

  /** Метод для объединения полей Account в поле для DTO. */
  default String combineNames(String firstName, String secondName) {
    return firstName + " " + secondName;
  }

  /** Маппинг для PUT запроса Account. */
  Account infoDtoToAccount(AccountInfoDto accountInfoDto);

  /** Маппинг для UPDATE запроса Account. */
  Account updateDtoToAccount(AccountUpdateDto accountUpdateDto);

  /** Маппинг для GET запроса Group. */
  GroupIdDto groupToIdDto(Group group);

  /** Маппинг Group. */
  GroupDto groupToDto(Group group);

  /** Маппинг для TODO запроса Group. */
  Group idDtoToGroup(GroupIdDto groupIdDto);

  /** Маппинг для TODO запроса Group. */
  Group dtoToGroup(GroupDto groupDto);

  /** Маппинг для POST, UPDATE запроса Group. */
  Group createDtoToGroup(GroupCreateDto groupCreateDto);

  /** Маппинг для возвращаемого значения POST запроса создания связи. */
  @Mapping(target = "groupId", expression = "java(accGroupLink.getGroup().getId())")
  @Mapping(target = "accountId", expression = "java(accGroupLink.getAccount().getId())")
  AccGroupLinkDto linkToDto(AccGroupLink accGroupLink);
}
