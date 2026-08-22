package studyweb.cus.security.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import studyweb.cus.constant.RedisConstants;
import studyweb.cus.entity.redis.RefreshToken;
import studyweb.cus.enums.UserRole;
import studyweb.cus.repository.auth.RefreshTokenRepository;
import studyweb.cus.security.JwtUtils;

@Component
@Slf4j
public class JwtUtilsImpl implements JwtUtils {

  private final SecretKey secretKey;
  private final long accessTokenExpiration;
  private final long refreshTokenExpiration;
  private final RefreshTokenRepository refreshTokenRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedisScript<Long> revokeRefreshTokenScript;

  public JwtUtilsImpl(
      @Value("${app.jwt.secret}") String jwtSecret,
      @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration,
      @Value("${app.jwt.refresh-token-expiration}") long refreshTokenExpiration,
      RefreshTokenRepository refreshTokenRepository,
      RedisTemplate<String, Object> redisTemplate,
      RedisScript<Long> revokeRefreshTokenScript) {

    this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    this.accessTokenExpiration = accessTokenExpiration;
    this.refreshTokenExpiration = refreshTokenExpiration;
    this.refreshTokenRepository = refreshTokenRepository;
    this.redisTemplate = redisTemplate;
    this.revokeRefreshTokenScript = revokeRefreshTokenScript;
    log.info("JwtUtils initialized successfully with Lua script support");
  }

  private String createToken(String email, UserRole role, long expiration) {
    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + expiration);

    JwtBuilder builder =
        Jwts.builder()
            .setSubject(email)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(secretKey, SignatureAlgorithm.HS512);
    if (role != null) {
      builder.claim("role", role.name());
    }
    return builder.compact();
  }

  @Override
  public String generateAccessToken(String email, UserRole role) {
    return createToken(email, role, accessTokenExpiration);
  }

  @Override
  public String generateRefreshToken(String email) {
    refreshTokenRepository.deleteByEmail(email);

    String refreshToken = createToken(email, null, refreshTokenExpiration);

    RefreshToken token =
        RefreshToken.builder()
            .refreshToken(refreshToken)
            .email(email)
            .ttl(TimeUnit.MILLISECONDS.toSeconds(refreshTokenExpiration))
            .build();
    refreshTokenRepository.save(token);

    log.debug("Generated and saved refresh token for user");
    return refreshToken;
  }

  @Override
  public void revokeSpecificRefreshToken(String refreshToken) {
    refreshTokenRepository.deleteById(refreshToken);
    log.info("Specific refresh token revoked");
  }

  @Override
  public void revokeAllSessions(String email) {
    try {
      Long deletedCount =
          redisTemplate.execute(
              revokeRefreshTokenScript,
              Collections.singletonList(RedisConstants.REFRESH_TOKEN_KEY_PATTERN),
              email);
      log.info("All sessions revoked for email: {} (deleted {} tokens)", email, deletedCount);
    } catch (Exception e) {
      log.warn(
          "Failed to revoke sessions using Lua script for email: {}, falling back to repository",
          email,
          e);
      refreshTokenRepository.deleteByEmail(email);
    }
  }

  private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
    final Claims claims =
        Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    return claimsResolver.apply(claims);
  }

  @Override
  public String getEmailFromToken(String token) {
    return getClaimFromToken(token, Claims::getSubject);
  }

  @Override
  public boolean validateToken(String token) {
    try {
      Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      log.warn("JWT validation error: {}", e.getMessage());
      return false;
    }
  }

  @Override
  public long getAccessTokenExpiration() {
    return accessTokenExpiration;
  }

  @Override
  public long getRefreshTokenExpiration() {
    return refreshTokenExpiration;
  }
}
