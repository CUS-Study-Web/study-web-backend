package studyweb.cus.aspect.activity;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import studyweb.cus.annotation.activity.LogActivity;
import studyweb.cus.dto.request.auth.LoginRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class ActivityLogAspect {

  private static final Logger activityLogger = LoggerFactory.getLogger("ACTIVITY_LOGGER");

  private final ApplicationContext applicationContext;
  private final ExpressionParser parser = new SpelExpressionParser();
  private final ParserContext templateContext = new TemplateParserContext();
  private final ParameterNameDiscoverer paramDiscoverer = new DefaultParameterNameDiscoverer();

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

    String rawDescription =
        logActivity.description().isEmpty()
            ? "Executed " + joinPoint.getSignature().getName()
            : logActivity.description();

    String description = evaluateDescription(rawDescription, joinPoint, result, auth);

    try {
      MDC.put("user_id", userId);
      MDC.put("action_type", logActivity.action().name());

      activityLogger.info(description);
    } finally {
      MDC.clear();
    }
  }

  private String evaluateDescription(
      String rawDescription, JoinPoint joinPoint, Object result, Authentication auth) {
    if (!rawDescription.contains("#{")) {
      return rawDescription;
    }
    try {
      StandardEvaluationContext context = new StandardEvaluationContext();
      context.setBeanResolver(new BeanFactoryResolver(applicationContext));

      if (joinPoint.getSignature() instanceof MethodSignature signature) {
        Method method = signature.getMethod();
        String[] paramNames = paramDiscoverer.getParameterNames(method);
        Object[] args = joinPoint.getArgs();
        if (paramNames != null && args != null) {
          for (int i = 0; i < paramNames.length && i < args.length; i++) {
            context.setVariable(paramNames[i], args[i]);
          }
        }
      }

      context.setVariable("result", result);
      context.setVariable("auth", auth);

      String evaluated =
          parser.parseExpression(rawDescription, templateContext).getValue(context, String.class);
      return evaluated != null ? evaluated : rawDescription;
    } catch (Exception e) {
      log.warn("Failed to evaluate SpEL description '{}': {}", rawDescription, e.getMessage());
      return rawDescription;
    }
  }
}
