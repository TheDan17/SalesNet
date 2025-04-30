package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.object.data.BulkResultDetailed;
import com.thedan17.salesnet.core.object.dto.AccountSignupDto;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.validation.validator.AccountLoginDtoValidator;
import com.thedan17.salesnet.util.CommonUtil;
import com.thedan17.salesnet.util.EntityMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Component
@Service
public class AccountBulkService {
  @Autowired
  AccountRepository accountRepository;
  EntityMapper entityMapper;

  public AccountBulkService (AccountRepository accountRepository, EntityMapper entityMapper) {
    this.accountRepository = accountRepository;
    this.entityMapper = entityMapper;
  }

  private Account makeAccount(AccountSignupDto accountSignupDto) {
    Account account = entityMapper.loginDtoToAccount(accountSignupDto);
    account.setPasswordHash(CommonUtil.hashWithSha256(accountSignupDto.getPassword()));
    return account;
  }

  @Transactional
  private void saveAccount(Account account) {
    accountRepository.save(account);
  }

  public BulkResultDetailed addAccountsBulk(List<AccountSignupDto> accountsDto) {
    BulkResultDetailed processResult = new BulkResultDetailed();
    IntStream.range(0, accountsDto.size())
            .mapToObj(dtoIndex -> {
              AccountSignupDto dto = accountsDto.get(dtoIndex);
              return Pair.of(
                  makeAccount(dto),
                  BulkResultDetailed.createResult(dtoIndex, dto, AccountLoginDtoValidator::validate)
              );
            })
            .peek(pair -> {
              processResult.addResult(pair.getSecond());
            })
            .filter(pair ->
                    pair.getSecond().getStatus() == BulkResultDetailed.BulkElementStatus.SUCCESS)
            .forEach(pair -> {
              try {
                saveAccount(pair.getFirst());
              } catch (RuntimeException e) {
                List<BulkResultDetailed.BulkElementError> errors = new ArrayList<>();

                String msg = ExceptionUtils.getRootCauseMessage(e);
                int dotIndex = msg.indexOf('.');
                msg = dotIndex > 0 ? msg.substring(0, msg.indexOf('.')).trim() : msg;

                errors.add(new BulkResultDetailed.BulkElementError("database_saving", msg));
                processResult.updateResult(new BulkResultDetailed.BulkElementResult(
                        pair.getSecond().getIndex(),
                        BulkResultDetailed.BulkElementStatus.FAILURE,
                        errors
                ));
              }
            });
    return processResult;
  }
}
