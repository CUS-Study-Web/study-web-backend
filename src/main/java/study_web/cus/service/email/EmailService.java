package study_web.cus.service.email;

public interface EmailService {

    void sendPasswordResetOtp(String toEmail, String otpCode, long expirationMinutes);
}
