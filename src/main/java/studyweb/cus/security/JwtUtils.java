package studyweb.cus.security;

import studyweb.cus.enums.UserRole;

public interface JwtUtils {

  String generateAccessToken(String email, UserRole role, boolean isVip);

  String generateRefreshToken(String email);

  String getEmailFromToken(String token);

  boolean getIsVipFromToken(String token);

  boolean validateToken(String token);

  long getAccessTokenExpiration();

  long getRefreshTokenExpiration();

  void revokeSpecificRefreshToken(String refreshToken);

  void revokeAllSessions(String email);
}
