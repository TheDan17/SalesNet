package com.thedan17.salesnet.dao;

import com.thedan17.salesnet.model.Group;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupSearchRepository extends JpaRepository<Group, Long> {
  @Query(
      value = "SELECT * FROM groups g WHERE g.name ILIKE '%' || :substr || '%';",
      nativeQuery = true)
  public Set<Group> findByNameSubstringPsql(@Param("substr") String substring);

  @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT(CONCAT('%', :substr), '%'))")
  public Set<Group> findByNameSubstringJpql(@Param("substr") String substring);

  @Query(
      value =
          "SELECT grp.* FROM (SELECT * FROM acc_group_link WHERE account_id = :accId) links INNER JOIN groups grp ON links.group_id=grp.id WHERE grp.name ILIKE '%' || :substr || '%';",
      nativeQuery = true)
  public Set<Group> findByNameInAccPsql(@Param("substr") String substr, @Param("accId") Long accId);

  @Query(
      "SELECT link.group FROM AccGroupLink link WHERE link.account.id = :accId AND LOWER(link.group.name) LIKE LOWER(CONCAT(CONCAT('%', :substr), '%'))")
  public Set<Group> findByNameInAccJpql(@Param("substr") String substr, @Param("accId") Long accId);
}
