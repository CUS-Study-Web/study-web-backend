package studyweb.cus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import studyweb.cus.converter.StringToUuidConverter;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

  private final StringToUuidConverter stringToUuidConverter;

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(stringToUuidConverter);
  }
}
