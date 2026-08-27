package studyweb.cus.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class YouTubeUrlValidator implements ConstraintValidator<ValidYouTubeUrl, String> {

  private static final Pattern YOUTUBE_PATTERN =
      Pattern.compile(
          "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?.*v=|embed/|v/|shorts/|live/)|youtu\\.be/)[a-zA-Z0-9_-]+(&.*|\\?.*)?$",
          Pattern.CASE_INSENSITIVE);

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null || value.trim().isEmpty()) {
      return false;
    }
    return YOUTUBE_PATTERN.matcher(value.trim()).matches();
  }
}
