package com.thedan17.salesnet;

import com.thedan17.salesnet.core.object.data.BulkResultDetailed;
import com.thedan17.salesnet.core.validation.ValidationError;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.util.Pair;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class BulkResultDetailedTests {
  List<ValidationError> createEmptyElementVErrors() {
    return new ArrayList<>();
  }
  List<ValidationError> createContainElementVErrors() {
    List<ValidationError> list = new ArrayList<>();
    list.add(new ValidationError("source_vpp", "Message vtext #"));
    list.add(new ValidationError("source_vep", "Message vtext /"));
    list.add(new ValidationError("source_vgp", "Message vtext ="));
    return list;
  }
  List<ValidationError> createAdditionalElementVErrors() {
    List<ValidationError> list = new ArrayList<>();
    list.add(new ValidationError("source_vno", "Message vanother"));
    list.add(new ValidationError("source_vsl", "Message vthe msg"));
    return list;
  }
  List<BulkResultDetailed.ElementError> createEmptyElementErrors() {
    return new ArrayList<>();
  }
  List<BulkResultDetailed.ElementError> createContainElementErrors() {
    List<BulkResultDetailed.ElementError> list = new ArrayList<>();
    list.add(new BulkResultDetailed.ElementError("source_kpp", "Message text #"));
    list.add(new BulkResultDetailed.ElementError("source_hep", "Message text /"));
    list.add(new BulkResultDetailed.ElementError("source_dgp", "Message text ="));
    return list;
  }
  BulkResultDetailed createHavingDataObject() {
    BulkResultDetailed bulkResultDetailed = new BulkResultDetailed();
    bulkResultDetailed.addResult(0, "item0", item -> createContainElementVErrors());
    bulkResultDetailed.addResult(1, "item1", item -> createEmptyElementVErrors());
    bulkResultDetailed.addResult(2, "item2", item -> createEmptyElementVErrors());
    bulkResultDetailed.addResult(3, "item3", item -> createContainElementVErrors());
    return bulkResultDetailed;
  }
  BulkResultDetailed.ElementResult createSuccessElementResult(long index) {
    return new BulkResultDetailed.ElementResult(
            index,
            BulkResultDetailed.ElementStatus.SUCCESS,
            createEmptyElementErrors()
    );
  }
  BulkResultDetailed.ElementResult createFailureElementResult(long index) {
    return new BulkResultDetailed.ElementResult(
            index,
            BulkResultDetailed.ElementStatus.FAILURE,
            createContainElementErrors()
    );
  }

  @Test
  void testNewObjectCorrectness() {
    BulkResultDetailed result = new BulkResultDetailed();
    assertEquals(0, result.getSuccessAmount());
    assertEquals(0, result.getFailureAmount());
    assertEquals(0, result.getTotalAmount());
    assertEquals(0, result.getFailurePercentage());
    assertEquals(0, result.getSuccessPercentage());
    assertNotEquals(null, result.getResults());
    assertEquals(0, result.getResults().size());
  }

  @Test
  void testAddResultCommon() {
    BulkResultDetailed resultWithData = createHavingDataObject();
    Integer currentTotal = resultWithData.getTotalAmount();
    Integer currentSuccess = resultWithData.getSuccessAmount();
    Integer currentFailure = resultWithData.getFailureAmount();

    resultWithData.addResult(createSuccessElementResult(currentTotal));
    assertEquals(currentTotal+1, resultWithData.getTotalAmount());
    assertEquals(currentSuccess+1, resultWithData.getSuccessAmount());
    assertEquals(currentFailure, resultWithData.getFailureAmount());

    resultWithData.addResult(createFailureElementResult(currentTotal+1L));
    assertEquals(currentTotal+2, resultWithData.getTotalAmount());
    assertEquals(currentSuccess+1, resultWithData.getSuccessAmount());
    assertEquals(currentFailure+1, resultWithData.getFailureAmount());
  }

  @Test
  void testCreateResultFunc() {
    Long index = 0L;
    BulkResultDetailed.ElementResult elementResult =
            BulkResultDetailed.createResult(index, "item", item->createEmptyElementVErrors());
    assertEquals(index, elementResult.getIndex());
    assertEquals(BulkResultDetailed.ElementStatus.SUCCESS, elementResult.getStatus());
    assertNotEquals(null, elementResult.getErrors());
    assertEquals(0, elementResult.getErrors().size());

    index = 1L;
    elementResult = BulkResultDetailed.createResult(
            index, "item", item->createAdditionalElementVErrors());
    assertEquals(index, elementResult.getIndex());
    assertEquals(BulkResultDetailed.ElementStatus.FAILURE, elementResult.getStatus());
    assertNotEquals(null, elementResult.getErrors());
    assertNotEquals(0, elementResult.getErrors().size());
  }

  @Test
  void testAddResultFunc() {
    BulkResultDetailed result = createHavingDataObject();

    Long index = (long) result.getResults().size();
    result.addResult(index, "item", item->createEmptyElementVErrors());
    var elementResult = result.getResults().get(Math.toIntExact(index));
    assertEquals(index, elementResult.getIndex());
    assertEquals(BulkResultDetailed.ElementStatus.SUCCESS, elementResult.getStatus());
    assertNotEquals(null, elementResult.getErrors());
    assertEquals(0, elementResult.getErrors().size());

    index += 1;
    result.addResult(index, "item", item->createAdditionalElementVErrors());
    elementResult = result.getResults().get(Math.toIntExact(index));
    assertEquals(index, elementResult.getIndex());
    assertEquals(BulkResultDetailed.ElementStatus.FAILURE, elementResult.getStatus());
    assertNotEquals(null, elementResult.getErrors());
    assertNotEquals(0, elementResult.getErrors().size());
  }

  @Test
  void testManyAddResultFunc() {
    BulkResultDetailed result = createHavingDataObject();

    long index = (long) result.getResults().size()-1;
    List<Pair<Long, String>> src = new ArrayList<>();
    src.add(Pair.of(index+1, "itemSuccess"));
    src.add(Pair.of(index+2, "itemFailure"));
    result.addResults(src, item->item.equals("itemSuccess")
            ? createEmptyElementVErrors()
            : createAdditionalElementVErrors()
    );

    var elementResult = result.getResults().get(Math.toIntExact(index+1));
    assertEquals(index+1, elementResult.getIndex());
    assertEquals(BulkResultDetailed.ElementStatus.SUCCESS, elementResult.getStatus());
    assertNotEquals(null, elementResult.getErrors());
    assertEquals(0, elementResult.getErrors().size());

    elementResult = result.getResults().get(Math.toIntExact(index+2));
    assertEquals(index+2, elementResult.getIndex());
    assertEquals(BulkResultDetailed.ElementStatus.FAILURE, elementResult.getStatus());
    assertNotEquals(null, elementResult.getErrors());
    assertNotEquals(0, elementResult.getErrors().size());
  }

  @Test
  void shouldUpdateWhenElemExist() {
    BulkResultDetailed original = createHavingDataObject();
    BulkResultDetailed modified = createHavingDataObject();
    modified.addResult(0, "item", item -> createEmptyElementVErrors());
    modified.addResult(1, "item", item -> createAdditionalElementVErrors());
    modified.addResult(3, "item", item -> createAdditionalElementVErrors());
    assertEquals(BulkResultDetailed.ElementStatus.FAILURE,
            modified.getResults().get(0).getStatus());
    assertEquals(BulkResultDetailed.ElementStatus.FAILURE,
            modified.getResults().get(1).getStatus());
    assertEquals(BulkResultDetailed.ElementStatus.FAILURE,
            modified.getResults().get(1).getStatus());
    var addErrorList = createAdditionalElementVErrors();
    assertEquals(
            original.getResults().get(0).getErrors().size(),
            modified.getResults().get(0).getErrors().size());
    assertEquals(
            original.getResults().get(1).getErrors().size() + addErrorList.size(),
            modified.getResults().get(1).getErrors().size());
    assertEquals(
            original.getResults().get(3).getErrors().size() + addErrorList.size(),
            modified.getResults().get(3).getErrors().size());
  }
}
