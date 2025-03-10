package com.thedan17.salesnet.service;

import com.thedan17.salesnet.dto.AccGroupLinkDto;
import com.thedan17.salesnet.dto.AccountInfoDto;
import com.thedan17.salesnet.dto.AccountLoginDto;
import com.thedan17.salesnet.dto.AccountUpdateDto;
import com.thedan17.salesnet.dto.GroupDto;
import com.thedan17.salesnet.dto.GroupIdDto;
import com.thedan17.salesnet.model.AccGroupLink;
import com.thedan17.salesnet.model.Account;
import com.thedan17.salesnet.model.Group;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Класс для преобразования всех объектов {@code SalesNet}. */
@Mapper(componentModel = "spring")
public interface MapperService {

  /** Перевод из AccountLogin в Account для POST запроса. */
  Account loginDtoToAccount(AccountLoginDto accountLoginDto);

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

  /** Маппинг для POST запроса Group. */
  Group idDtoToGroup(GroupIdDto groupIdDto);

  /** Маппинг для UPDATE запроса Group. */
  Group dtoToGroup(GroupDto groupDto);

  /** Маппинг для возвращаемого значения POST запроса создания связи. */
  @Mapping(target = "groupId", expression = "java(accGroupLink.getGroup().getId())")
  @Mapping(target = "accountId", expression = "java(accGroupLink.getAccount().getId())")
  AccGroupLinkDto linkToDto(AccGroupLink accGroupLink);
}
