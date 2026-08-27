package studyweb.cus.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StringToUuidConverterTest {

  private StringToUuidConverter converter;

  @BeforeEach
  void setUp() {
    converter = new StringToUuidConverter();
  }

  @Test
  @DisplayName("Standard UUID string converts properly")
  void shouldConvertStandardUuid() {
    UUID uuid = UUID.randomUUID();
    UUID result = converter.convert(uuid.toString());
    assertThat(result).isEqualTo(uuid);
  }

  @Test
  @DisplayName("UUID with surrounding whitespace converts properly")
  void shouldConvertUuidWithWhitespace() {
    UUID uuid = UUID.randomUUID();
    UUID result = converter.convert("   " + uuid + "   ");
    assertThat(result).isEqualTo(uuid);
  }

  @Test
  @DisplayName("UUID surrounded by brackets converts properly")
  void shouldConvertUuidWithBrackets() {
    UUID uuid = UUID.randomUUID();
    UUID result = converter.convert("[" + uuid + "]");
    assertThat(result).isEqualTo(uuid);
  }

  @Test
  @DisplayName("UUID surrounded by quotes and brackets (JSON array element format) converts properly")
  void shouldConvertUuidWithQuotesAndBrackets() {
    UUID uuid = UUID.randomUUID();
    UUID result = converter.convert("[\"" + uuid + "\"]");
    assertThat(result).isEqualTo(uuid);

    UUID resultSingleQuote = converter.convert("['" + uuid + "']");
    assertThat(resultSingleQuote).isEqualTo(uuid);
  }

  @Test
  @DisplayName("First or last element of comma-split array string converts properly")
  void shouldConvertSplitArrayTokens() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    // Spring splits "[\"uuid1\", \"uuid2\"]" by comma into:
    // Token 1: "[\"uuid1\""
    // Token 2: " \"uuid2\"]"
    UUID result1 = converter.convert("[\"" + uuid1 + "\"");
    UUID result2 = converter.convert(" \"" + uuid2 + "\"]");

    assertThat(result1).isEqualTo(uuid1);
    assertThat(result2).isEqualTo(uuid2);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "[]", "[\"\"]", "null", "undefined", "[null]"})
  @DisplayName("Empty, blank, empty array, or null-like strings return null")
  void shouldReturnNullForEmptyOrBlank(String input) {
    assertThat(converter.convert(input)).isNull();
  }

  @Test
  @DisplayName("Invalid UUID string throws IllegalArgumentException")
  void shouldThrowForInvalidUuid() {
    assertThatThrownBy(() -> converter.convert("invalid-uuid-string"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
