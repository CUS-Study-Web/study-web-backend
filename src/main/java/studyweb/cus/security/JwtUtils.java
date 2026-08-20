package studyweb.cus.security;

import studyweb.cus.enums.UserRole;

public interface JwtUtils {

  String generateAccessToken(String email, UserRole role);

  String generateRefreshToken(String email);

  String getEmailFromToken(String token);

  boolean validateToken(String token);

  long getAccessTokenExpiration();

  long getRefreshTokenExpiration();

  void revokeSpecificRefreshToken(String refreshToken);

  void revokeAllSessions(String email);
}
