package com.thedan17.salesnet;

import com.thedan17.salesnet.dto.GroupIdDto;
import com.thedan17.salesnet.model.Group;
import com.thedan17.salesnet.util.MapperService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class MapperServiceTests {
  @Autowired MapperService mapperService;

  Group createTestGroup() {
    Long groupId = 15L;
    String groupName = "group name", groupDescription = "group description";
    Group group = new Group(groupName, groupDescription);
    group.setId(groupId);
    return group;
  }
  @Test
  public void groupToIdDto_whenGroupValid_returnsNotNullDtoFields() {
    Group group = createTestGroup();
    GroupIdDto groupIdDto = mapperService.groupToIdDto(group);
    assertThat(groupIdDto).hasNoNullFieldsOrProperties();
  }
  @Test
  public void groupToIdDto_whenGroupValid_returnsDto() {
    Group group = createTestGroup();
    GroupIdDto groupIdDto = mapperService.groupToIdDto(group);
    assertThat(groupIdDto)
        .usingRecursiveComparison()
        .comparingOnlyFields("id", "name", "description", "createdAt")
        .isEqualTo(group);
  }

  @Test
  public void groupToIdDto_whenGroupIsNull_returnsNull() {
    Group group = null;
    GroupIdDto groupIdDto = mapperService.groupToIdDto(group);
    assertNull(groupIdDto);
  }
}
