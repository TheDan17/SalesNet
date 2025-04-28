package com.thedan17.salesnet.core.service;

import com.thedan17.salesnet.core.dao.AccountRepository;
import com.thedan17.salesnet.core.object.data.BulkOperationResultDetailed;
import com.thedan17.salesnet.core.object.dto.AccountLoginDto;
import com.thedan17.salesnet.util.EntityMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

  @Transactional
  public BulkOperationResultDetailed addAccountsBulk(List<AccountLoginDto> accountsDto) {
    BulkOperationResultDetailed processResult = new BulkOperationResultDetailed();


    return processResult;
  }


}
