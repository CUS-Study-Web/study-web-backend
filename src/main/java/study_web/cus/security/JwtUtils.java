package study_web.cus.security;

public interface JwtUtils {

  String generateAccessToken(String email);

  String generateRefreshToken(String email);

  String getEmailFromToken(String token);

  boolean validateToken(String token);

  long getAccessTokenExpiration();

  long getRefreshTokenExpiration();

  void revokeSpecificRefreshToken(String refreshToken);

  void revokeAllSessions(String email);
}
