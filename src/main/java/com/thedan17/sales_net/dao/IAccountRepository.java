package com.thedan17.sales_net.dao;

import com.thedan17.sales_net.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface IAccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {}
