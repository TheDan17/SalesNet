package com.thedan17.salesnet;

import com.thedan17.salesnet.util.PasswordCheckUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class PasswordCheckUtilTests {
  private static Stream<Arguments> provideUppercaseTestData() {
    return Stream.of(
      Arguments.of("Test", true),
      Arguments.of("test", false),
      Arguments.of("1234", false),
      Arguments.of("hIkEr", true),
      Arguments.of("pokeR", true),
      Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideUppercaseTestData")
  void testPasswordContainUppercase(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.containsUpperCase(password);
    assertEquals(resultValue, expectedValue);
  }

  private static Stream<Arguments> provideLowercaseTestData() {
    return Stream.of(
            Arguments.of("Test", true),
            Arguments.of("test", true),
            Arguments.of("1234", false),
            Arguments.of("TEST", false),
            Arguments.of("REsT", true),
            Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideLowercaseTestData")
  void testPasswordContainLowercase(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.containsLowerCase(password);
    assertEquals(resultValue, expectedValue);
  }

  private static Stream<Arguments> provideDigitTestData() {
    return Stream.of(
            Arguments.of("Test", false),
            Arguments.of("test", false),
            Arguments.of("1234", true),
            Arguments.of("TEST3", true),
            Arguments.of("REs_ss#@5T", true),
            Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideDigitTestData")
  void testPasswordContainDigit(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.containsDigit(password);
    assertEquals(resultValue, expectedValue);
  }
}
