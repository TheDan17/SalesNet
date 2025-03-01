package com.thedan17.sales_net.service;

import com.thedan17.sales_net.dao.IAccountRepository;
import com.thedan17.sales_net.model.Account;

import java.util.ArrayList;
import java.util.Optional;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AccountService {
    private final IAccountRepository dao;

    public AccountService(IAccountRepository repository) {
        this.dao = repository;
    }

    public Optional<Account> getAccountById(Long id) {
        return dao.findById(id);
    }

    public Account addAccount(Account account) {
        return dao.save(account);
    }

    public List<Account> searchAccounts(String firstName, String secondName, String type) {
        Specification<Account> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (firstName != null){
                predicates.add(criteriaBuilder.equal(root.get("firstName"), firstName));
            }
            if (secondName != null){
                predicates.add(criteriaBuilder.equal(root.get("secondName"), secondName));
            }
            if (type != null){
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        return dao.findAll(spec);
    }
}
