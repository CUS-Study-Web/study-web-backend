package study_web.cus.security;

import study_web.cus.enums.Role;

public interface JwtUtils {

    String generateAccessToken(String email, Role role);

    String generateRefreshToken(String email);

    String getEmailFromToken(String token);

    Role getRoleFromToken(String token);

    boolean validateToken(String token);

    long getAccessTokenExpiration();

    long getRefreshTokenExpiration();

    void revokeSpecificRefreshToken(String refreshToken);

    void revokeAllSessions(String email);
}
