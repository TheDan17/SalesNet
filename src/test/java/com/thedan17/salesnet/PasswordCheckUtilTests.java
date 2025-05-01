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

  private static Stream<Arguments> provideKeyboardSequenceData() {
    return Stream.of(
            Arguments.of("JustPassword3643", false),
            Arguments.of("LolQweRty", true),
            Arguments.of("mnbvcx102938", true),
            Arguments.of("qw_er_ty_12345678", false),
            Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideKeyboardSequenceData")
  void testPasswordContainKeyboardSequence(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.containsKeyboardSequence(
            password, PasswordCheckUtil.REQUIRED_SEQUENCE_LENGTH_DETECT);
    assertEquals(resultValue, expectedValue);
  }

  private static Stream<Arguments> provideNumericSequenceData() {
    return Stream.of(
            Arguments.of("JustPassword364333", false),
            Arguments.of("LolQweRty4321", true),
            Arguments.of("mnbvcx12034056", false),
            Arguments.of("12345678", true),
            Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideNumericSequenceData")
  void testPasswordContainNumericSequence(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.containsNumericSequence(
            password, PasswordCheckUtil.REQUIRED_SEQUENCE_LENGTH_DETECT);
    assertEquals(resultValue, expectedValue);
  }

  private static Stream<Arguments> provideAllowedCharsData() {
    return Stream.of(
            Arguments.of("JustPassword+=0", false),
            Arguments.of("The_Password_12345", true),
            Arguments.of("Lets#Go#666666", false),
            Arguments.of("Русский_пароль", false),
            Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideAllowedCharsData")
  void testPasswordContainAllowedChars(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.containsOnlyAllowedChars(password);
    assertEquals(resultValue, expectedValue);
  }

  private static Stream<Arguments> provideRepeatingData() {
    return Stream.of(
            Arguments.of("JustPassword36433", true),
            Arguments.of("Lets#Go#666666", false),
            Arguments.of("tttrip", false),
            Arguments.of("", true)
    );
  }

  @ParameterizedTest
  @MethodSource("provideRepeatingData")
  void testPasswordContainsRepeating(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.doesNotContainRepeatedChars(password);
    assertEquals(resultValue, expectedValue);
  }

  private static Stream<Arguments> provideLengthData() {
    return Stream.of(
            Arguments.of("1234567", false),
            Arguments.of("12345678", true),
            Arguments.of("123456789", true),
            Arguments.of("", false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideLengthData")
  void testPasswordHaveEnoughLength(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.haveEnoughLength(password);
    assertEquals(resultValue, expectedValue);
  }

  private static Stream<Arguments> provideSequenceTestData() {
    return Stream.of(
            Arguments.of("JustPassword3643333", true),
            Arguments.of("LolQweRty", false),
            Arguments.of("87654321", false),
            Arguments.of("JustPassword3643", true),
            Arguments.of("LolQweRty4321", false),
            Arguments.of("mnbvcx102938", false),
            Arguments.of("qw_er_ty_12_34_43_21", true),
            Arguments.of("11", true),
            Arguments.of("", true)
    );
  }

  @ParameterizedTest
  @MethodSource("provideSequenceTestData")
  void testPasswordContainSequences(String password, boolean expectedValue) {
    boolean resultValue = PasswordCheckUtil.doesNotContainSequences(password);
    assertEquals(resultValue, expectedValue);
  }
}
