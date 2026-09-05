package studyweb.cus.aspect.activity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.lang.reflect.Method;
import java.time.LocalDate;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import studyweb.cus.annotation.activity.LogActivity;
import studyweb.cus.dto.request.auth.LoginRequest;
import studyweb.cus.dto.request.auth.RegisterRequest;
import studyweb.cus.enums.ActionType;
import studyweb.cus.enums.Gender;

@ExtendWith(MockitoExtension.class)
class ActivityLogAspectTest {

  @Mock private ApplicationContext applicationContext;
  @Mock private JoinPoint joinPoint;
  @Mock private MethodSignature methodSignature;
  @Mock private LogActivity logActivity;

  private ActivityLogAspect activityLogAspect;
  private ListAppender<ILoggingEvent> listAppender;
  private Logger activityLogger;

  @BeforeEach
  void setUp() {
    activityLogAspect = new ActivityLogAspect(applicationContext);
    activityLogger = (Logger) LoggerFactory.getLogger("ACTIVITY_LOGGER");
    listAppender = new ListAppender<>();
    listAppender.start();
    activityLogger.addAppender(listAppender);
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  @AfterEach
  void tearDown() {
    activityLogger.detachAppender(listAppender);
    listAppender.stop();
    SecurityContextHolder.clearContext();
    MDC.clear();
  }

  // Dummy target method for reflection signature
  @SuppressWarnings("unused")
  public void sampleMethod(String targetName) {}

  @Test
  @DisplayName("handleActivityLog logs authenticated user and simple description")
  void handleActivityLog_authenticatedUser() {
    Authentication auth = mock(Authentication.class);
    when(auth.isAuthenticated()).thenReturn(true);
    when(auth.getName()).thenReturn("authuser@studyweb.edu");

    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(auth);
    SecurityContextHolder.setContext(securityContext);

    when(logActivity.action()).thenReturn(ActionType.LOGIN);
    when(logActivity.description()).thenReturn("User logged in successfully");

    activityLogAspect.handleActivityLog(joinPoint, logActivity, null);

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.get(0);
    assertThat(event.getMessage()).isEqualTo("User logged in successfully");
    assertThat(event.getMDCPropertyMap().get("user_id")).isEqualTo("authuser@studyweb.edu");
    assertThat(event.getMDCPropertyMap().get("action_type")).isEqualTo(ActionType.LOGIN.name());
    assertThat(MDC.get("user_id")).isNull();
  }

  @Test
  @DisplayName("handleActivityLog extracts email from LoginRequest when unauthenticated")
  void handleActivityLog_unauthenticated_loginRequest() {
    LoginRequest loginRequest = new LoginRequest("loginuser@studyweb.edu", "pass12345");
    when(joinPoint.getArgs()).thenReturn(new Object[] {loginRequest});
    when(logActivity.action()).thenReturn(ActionType.LOGIN);
    when(logActivity.description()).thenReturn("Login attempt");

    activityLogAspect.handleActivityLog(joinPoint, logActivity, null);

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.get(0);
    assertThat(event.getMDCPropertyMap().get("user_id")).isEqualTo("loginuser@studyweb.edu");
    assertThat(event.getMDCPropertyMap().get("action_type")).isEqualTo(ActionType.LOGIN.name());
  }

  @Test
  @DisplayName("handleActivityLog extracts email from RegisterRequest when unauthenticated")
  void handleActivityLog_unauthenticated_registerRequest() {
    RegisterRequest registerRequest =
        new RegisterRequest(
            "reguser@studyweb.edu",
            "Tien",
            "0901234567",
            LocalDate.of(2000, 1, 1),
            Gender.MALE,
            "School",
            "pass12345");
    when(joinPoint.getArgs()).thenReturn(new Object[] {registerRequest});
    when(logActivity.action()).thenReturn(ActionType.REGISTER);
    when(logActivity.description()).thenReturn("Register attempt");

    activityLogAspect.handleActivityLog(joinPoint, logActivity, null);

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.get(0);
    assertThat(event.getMDCPropertyMap().get("user_id")).isEqualTo("reguser@studyweb.edu");
    assertThat(event.getMDCPropertyMap().get("action_type")).isEqualTo(ActionType.REGISTER.name());
  }

  @Test
  @DisplayName("handleActivityLog defaults userId to ANONYMOUS when unauthenticated and no auth request in args")
  void handleActivityLog_unauthenticated_anonymous() {
    when(joinPoint.getArgs()).thenReturn(new Object[] {"non-auth-arg"});
    when(logActivity.action()).thenReturn(ActionType.REQUEST_VIP);
    when(logActivity.description()).thenReturn("Starting exam");

    activityLogAspect.handleActivityLog(joinPoint, logActivity, null);

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.get(0);
    assertThat(event.getMDCPropertyMap().get("user_id")).isEqualTo("ANONYMOUS");
  }

  @Test
  @DisplayName("handleActivityLog defaults description to 'Executed <methodName>' when description is empty")
  void handleActivityLog_emptyDescription() {
    when(joinPoint.getArgs()).thenReturn(new Object[] {});
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getName()).thenReturn("submitAssessment");
    when(logActivity.action()).thenReturn(ActionType.SUBMIT_ASSESSMENT);
    when(logActivity.description()).thenReturn("");

    activityLogAspect.handleActivityLog(joinPoint, logActivity, null);

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.get(0);
    assertThat(event.getMessage()).isEqualTo("Executed submitAssessment");
  }

  @Test
  @DisplayName("handleActivityLog evaluates SpEL expression with arguments and result")
  void handleActivityLog_evaluatesSpelExpression() throws NoSuchMethodException {
    Method method = ActivityLogAspectTest.class.getMethod("sampleMethod", String.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);
    when(joinPoint.getArgs()).thenReturn(new Object[] {"Calculus"});

    when(logActivity.action()).thenReturn(ActionType.CREATE_LESSON);
    when(logActivity.description()).thenReturn("Created lesson: #{#targetName} result: #{#result}");

    activityLogAspect.handleActivityLog(joinPoint, logActivity, "SUCCESS_RESULT");

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.get(0);
    assertThat(event.getMessage()).isEqualTo("Created lesson: Calculus result: SUCCESS_RESULT");
  }

  @Test
  @DisplayName("handleActivityLog falls back to raw description when SpEL throws evaluation error")
  void handleActivityLog_spelErrorFallsBackToRaw() throws NoSuchMethodException {
    Method method = ActivityLogAspectTest.class.getMethod("sampleMethod", String.class);
    when(joinPoint.getSignature()).thenReturn(methodSignature);
    when(methodSignature.getMethod()).thenReturn(method);
    when(joinPoint.getArgs()).thenReturn(new Object[] {"Calculus"});

    String badSpel = "#{1 / 0}";
    when(logActivity.action()).thenReturn(ActionType.CREATE_LESSON);
    when(logActivity.description()).thenReturn(badSpel);

    activityLogAspect.handleActivityLog(joinPoint, logActivity, null);

    assertThat(listAppender.list).hasSize(1);
    ILoggingEvent event = listAppender.list.get(0);
    assertThat(event.getMessage()).isEqualTo(badSpel);
  }
}
