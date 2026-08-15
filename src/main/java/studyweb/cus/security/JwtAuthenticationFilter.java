package studyweb.cus.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;

  private String userJwt;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    try {
      String jwt = extractJwtFromRequest(request);
      if (StringUtils.hasText(jwt) && jwtUtils.validateToken(jwt)) {
        String email = jwtUtils.getEmailFromToken(jwt);
        this.userJwt = jwt;

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Set authentication for email: {}", email);
      }
    } catch (Exception e) {
      log.warn("Authentication binding failed: {}", e.getMessage());
      request.setAttribute("auth_error", e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return path.startsWith("/ws");
  }

  private String extractJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  public String getUserJwt() {
    return this.userJwt;
  }
}
