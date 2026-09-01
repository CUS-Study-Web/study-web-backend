package studyweb.cus.aspect.activity;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import studyweb.cus.annotation.activity.LogActivity;
import studyweb.cus.dto.request.auth.LoginRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;

@Aspect
@Component
@Slf4j
public class ActivityLogAspect {

  private static final Logger activityLogger = LoggerFactory.getLogger("ACTIVITY_LOGGER");

  @AfterReturning(pointcut = "@annotation(logActivity)", returning = "result")
  public void handleActivityLog(JoinPoint joinPoint, LogActivity logActivity, Object result) {
    String userId = "ANONYMOUS";
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated()) {
      userId = auth.getName();
    } else {
      for (Object arg : joinPoint.getArgs()) {
        if (arg instanceof LoginRequest loginReq) {
          userId = loginReq.gmail();
          break;
        } else if (arg instanceof RegisterRequest registerReq) {
          userId = registerReq.gmail();
          break;
        }
      }
    }

    String description =
        logActivity.description().isEmpty()
            ? "Executed " + joinPoint.getSignature().getName()
            : logActivity.description();

    try {
      MDC.put("user_id", userId);
      MDC.put("action_type", logActivity.action().name());

      activityLogger.info(description);
    } finally {
      MDC.clear();
    }
  }
}
