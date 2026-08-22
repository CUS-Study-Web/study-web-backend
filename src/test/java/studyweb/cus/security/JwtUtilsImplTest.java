package studyweb.cus.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import studyweb.cus.enums.UserRole;
import studyweb.cus.repository.auth.RefreshTokenRepository;
import studyweb.cus.security.impl.JwtUtilsImpl;

@ExtendWith(MockitoExtension.class)
class JwtUtilsImplTest {

  private static final String SECRET = "unit-test-secret-key-for-hs512-signing-0123456789abcdef0123456789abcdef";

  @Mock private RefreshTokenRepository refreshTokenRepository;
  @Mock private RedisTemplate<String, Object> redisTemplate;
  @Mock private RedisScript<Long> revokeRefreshTokenScript;

  private JwtUtilsImpl jwtUtils;

  @BeforeEach
  void setUp() {
    jwtUtils =
        new JwtUtilsImpl(
            SECRET, 3_600_000L, 604_800_000L, refreshTokenRepository, redisTemplate,
            revokeRefreshTokenScript);
  }

  @Test
  void accessToken_carriesIsVipClaim() {
    String token = jwtUtils.generateAccessToken("a@b.c", UserRole.LEARNER, true);

    assertThat(jwtUtils.validateToken(token)).isTrue();
    assertThat(jwtUtils.getIsVipFromToken(token)).isTrue();
  }

  @Test
  void accessToken_nonVipClaimParsesFalse() {
    String token = jwtUtils.generateAccessToken("a@b.c", UserRole.LEARNER, false);

    assertThat(jwtUtils.getIsVipFromToken(token)).isFalse();
  }

  @Test
  void refreshToken_hasNoVipClaim() {
    String token = jwtUtils.generateRefreshToken("a@b.c");

    assertThat(jwtUtils.validateToken(token)).isTrue();
    assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo("a@b.c");
    assertThat(jwtUtils.getIsVipFromToken(token)).isFalse();
  }
}
