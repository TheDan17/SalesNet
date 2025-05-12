package com.thedan17.salesnet;

import com.thedan17.salesnet.util.CacheIdManager;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CacheIdManagerTests {
  @Test
  void shouldThrowWhenArgsInvalid() {
    assertThrows(IllegalArgumentException.class,
            () -> new CacheIdManager<Long, String, Long>(
                    obj -> 0L,
                    100L,
                    (short) 101
            )
    );
    assertThrows(IllegalArgumentException.class,
            () -> new CacheIdManager<Long, String, Long>(
                    obj -> 0L,
                    100L,
                    (short) 0
            )
    );
    assertDoesNotThrow(
            () -> new CacheIdManager<Long, String, Long>(
                    obj -> 0L,
                    100L,
                    (short) 1
            )
    );
    assertDoesNotThrow(
            () -> new CacheIdManager<Long, String, Long>(
                    obj -> 0L,
                    100L,
                    (short) 100
            )
    );
    assertDoesNotThrow(
            () -> new CacheIdManager<Long, String, Long>(
                    obj -> 0L,
                    100L,
                    (short) 50
            )
    );
  }


}
