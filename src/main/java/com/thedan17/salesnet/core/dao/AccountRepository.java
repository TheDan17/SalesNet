package com.thedan17.salesnet.core.dao;

import com.thedan17.salesnet.core.object.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Интерфейс для хранения Entity {@code Account} в базе данных. */
public interface AccountRepository extends JpaRepository<Account, Long>,
        JpaSpecificationExecutor<Account> {}
