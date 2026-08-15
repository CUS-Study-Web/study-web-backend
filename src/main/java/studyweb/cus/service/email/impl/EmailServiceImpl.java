package studyweb.cus.service.email.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import studyweb.cus.service.email.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

  private static final String PASSWORD_RESET_TEMPLATE = "static/email/password-reset.html";

  private final JavaMailSender mailSender;

  @Value("${spring.mail.from:}")
  private String mailFrom;

  @Override
  @Async
  public void sendPasswordResetOtp(String toEmail, String otpCode, long expirationMinutes) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, StandardCharsets.UTF_8.name());
      helper.setTo(toEmail);
      if (StringUtils.hasText(mailFrom)) {
        helper.setFrom(mailFrom);
      }
      helper.setSubject("CUS - Mã OTP đặt lại mật khẩu");

      String html =
          new ClassPathResource(PASSWORD_RESET_TEMPLATE)
              .getContentAsString(StandardCharsets.UTF_8)
              .replace("{{otp}}", otpCode)
              .replace("{{expiryMinutes}}", String.valueOf(expirationMinutes));
      helper.setText(html, true);

      mailSender.send(message);
      log.info("Sent password reset OTP email to: {}", toEmail);
    } catch (MessagingException | IOException e) {
      log.error("Failed to send password reset OTP email to: {}", toEmail, e);
    }
  }
}
