package com.thedan17.salesnet;

import com.thedan17.salesnet.core.object.dto.GroupIdDto;
import com.thedan17.salesnet.core.object.entity.Group;
import com.thedan17.salesnet.util.EntityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class EntityMapperTests {
  @Autowired
  EntityMapper entityMapper;

  Group createTestGroup() {
    Long groupId = 15L;
    String groupName = "group name";
    String groupDescription = "group description";
    Group group = new Group(groupName, groupDescription);
    group.setOwnerId(-1L);
    group.setId(groupId);
    return group;
  }
  @Test
  public void shouldReturnDtoHasNoNullFieldsWhenGroupIsValid() {
    Group group = createTestGroup();
    GroupIdDto groupIdDto = entityMapper.groupToIdDto(group);
    assertThat(groupIdDto).hasNoNullFieldsOrProperties();
  }
  @Test
  public void shouldReturnEqualDtoWhenGroupIsValid() {
    Group group = createTestGroup();
    GroupIdDto groupIdDto = entityMapper.groupToIdDto(group);
    assertThat(groupIdDto)
        .usingRecursiveComparison()
        .comparingOnlyFields("id", "name", "description", "createdAt")
        .isEqualTo(group);
  }

  @Test
  public void shouldReturnNullWhenGroupIsNull() {
    Group group = null;
    GroupIdDto groupIdDto = entityMapper.groupToIdDto(group);
    assertNull(groupIdDto);
  }
}
