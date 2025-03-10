package com.thedan17.salesnet.dao;

import com.thedan17.salesnet.model.AccGroupLink;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Интерфейс для хранения Entity {@code AccGroupLink} в базе данных. */
public interface AccGroupLinkRepository extends JpaRepository<AccGroupLink, Long> {
  /** Определение дополнительного метода интерфейса, который сгенерирует Spring. */
  Optional<AccGroupLink> findByAccountIdAndGroupId(Long accountId, Long groupId);
}
