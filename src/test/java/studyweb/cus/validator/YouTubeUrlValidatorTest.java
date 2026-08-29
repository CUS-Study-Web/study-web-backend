package studyweb.cus.validator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class YouTubeUrlValidatorTest {

  private YouTubeUrlValidator validator;

  @BeforeEach
  void setUp() {
    validator = new YouTubeUrlValidator();
  }

  @Test
  @DisplayName("Null value should pass validation")
  void shouldAcceptNull() {
    assertThat(validator.isValid(null, null)).isTrue();
  }

  @Test
  @DisplayName("Empty or blank string should fail validation")
  void shouldRejectEmptyOrBlank() {
    assertThat(validator.isValid("", null)).isFalse();
    assertThat(validator.isValid("   ", null)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "http://www.youtube.com/watch?v=dQw4w9WgXcQ",
        "https://youtube.com/watch?v=dQw4w9WgXcQ",
        "https://m.youtube.com/watch?v=dQw4w9WgXcQ",
        "https://youtu.be/dQw4w9WgXcQ",
        "https://www.youtube.com/embed/dQw4w9WgXcQ",
        "https://www.youtube.com/v/dQw4w9WgXcQ",
        "https://www.youtube.com/shorts/dQw4w9WgXcQ",
        "https://www.youtube.com/live/dQw4w9WgXcQ",
        "https://youtube.com/watch?v=123",
        "https://youtube.com/watch?v=dQw4w9WgXcQ&t=10s",
        "https://youtu.be/dQw4w9WgXcQ?si=abc123XYZ",
        "www.youtube.com/watch?v=dQw4w9WgXcQ",
        "youtu.be/dQw4w9WgXcQ"
      })
  @DisplayName("Valid YouTube URL patterns should pass validation")
  void shouldAcceptValidYouTubeUrls(String url) {
    assertThat(validator.isValid(url, null)).isTrue();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://notyoutube.com/watch?v=dQw4w9WgXcQ",
        "https://vimeo.com/12345678",
        "ftp://youtube.com/watch?v=dQw4w9WgXcQ",
        "random-text-here",
        "https://google.com"
      })
  @DisplayName("Invalid YouTube URL patterns should fail validation")
  void shouldRejectInvalidYouTubeUrls(String url) {
    assertThat(validator.isValid(url, null)).isFalse();
  }
}
