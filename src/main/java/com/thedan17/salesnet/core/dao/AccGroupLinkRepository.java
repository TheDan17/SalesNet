package com.thedan17.salesnet.core.dao;

import com.thedan17.salesnet.core.object.entity.AccGroupLink;
import com.thedan17.salesnet.core.object.entity.Account;
import com.thedan17.salesnet.core.object.entity.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Интерфейс для хранения Entity {@code AccGroupLink} в базе данных. */
public interface AccGroupLinkRepository extends JpaRepository<AccGroupLink, Long> {
  /** Определение дополнительного метода интерфейса, который сгенерирует Spring. */
  Optional<AccGroupLink> findByAccountIdAndGroupId(Long accountId, Long groupId);

  List<AccGroupLink> findByAccount(Account account);

  List<AccGroupLink> findByGroup(Group group);
}
