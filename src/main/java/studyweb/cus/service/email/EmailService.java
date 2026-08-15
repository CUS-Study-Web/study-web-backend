package studyweb.cus.service.email;

public interface EmailService {

  void sendPasswordResetOtp(String toEmail, String otpCode, long expirationMinutes);
}
