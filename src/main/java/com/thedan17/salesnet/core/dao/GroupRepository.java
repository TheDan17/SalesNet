package com.thedan17.salesnet.core.dao;

import com.thedan17.salesnet.core.object.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/** Интерфейс для хранения Entity {@code Group} в базе данных. */
public interface GroupRepository extends JpaRepository<Group, Long>,
        JpaSpecificationExecutor<Group> {}
