package com.thedan17.salesnet;

import com.thedan17.salesnet.util.CommonUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CommonUtilTests {
  @Test
  public void shouldMakeTrueSha256String() {
    List<String> sources = new ArrayList<>();
    sources.add("just example string");
    sources.add("Latin_UP_down_132");
    sources.add("Русский текст");
    sources.add("");
    List<String> matches = new ArrayList<>();
    matches.add("fb85e1efe38f6466bc3114620d9f89a8bef1b6e3d69ace411fde0ed0722c5fcb");
    matches.add("7fc0af98eeda6337e218ace579ddae13d3d2f96ac1204914055cc4fdcec5231c");
    matches.add("71a631c26669714ea79a98c183253778f2b4488f41ef08fe79cdbe4939b91b6e");
    matches.add("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    List<String> finalSources = sources.stream().map(CommonUtil::hashWithSha256).toList();
    IntStream.range(0, finalSources.size()).forEach(i -> {
            assertEquals(finalSources.get(i), matches.get(i));
    });
  }
}
