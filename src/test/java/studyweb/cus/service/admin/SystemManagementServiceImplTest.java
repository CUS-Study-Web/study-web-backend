package studyweb.cus.service.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import studyweb.cus.config.LokiProperties;
import studyweb.cus.dto.response.admin.DailyStatsResponse;
import studyweb.cus.dto.response.admin.LokiQueryRangeResponse;
import studyweb.cus.dto.response.admin.LokiQueryRangeResponse.LokiData;
import studyweb.cus.dto.response.admin.LokiQueryRangeResponse.LokiResultItem;
import studyweb.cus.dto.response.admin.MonthlyStatsResponse;
import studyweb.cus.enums.ActionType;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.mapper.admin.SystemManagementMapper;
import studyweb.cus.repository.content.PricingPageContentRepository;
import studyweb.cus.repository.course.AnswerKeyRepository;
import studyweb.cus.repository.course.AssessmentAttemptRepository;
import studyweb.cus.repository.course.AssessmentRepository;
import studyweb.cus.repository.course.UserCourseProgressRepository;
import studyweb.cus.repository.user.UserRepository;
import studyweb.cus.repository.user.VipRequestRepository;
import studyweb.cus.security.JwtUtils;
import studyweb.cus.service.admin.impl.SystemManagementServiceImpl;
import studyweb.cus.service.log.LokiQueryService;

@ExtendWith(MockitoExtension.class)
class SystemManagementServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private UserCourseProgressRepository userCourseProgressRepository;
  @Mock private AssessmentAttemptRepository assessmentAttemptRepository;
  @Mock private AnswerKeyRepository answerKeyRepository;
  @Mock private AssessmentRepository assessmentRepository;
  @Mock private VipRequestRepository vipRequestRepository;
  @Mock private PricingPageContentRepository pricingPageContentRepository;
  @Mock private SystemManagementMapper systemManagementMapper;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtUtils jwtUtils;
  @Mock private LokiQueryService lokiQueryService;
  @Mock private LokiProperties lokiProperties;

  @InjectMocks private SystemManagementServiceImpl systemManagementService;

  @Test
  @DisplayName("getDailyStats rejects end date before 1970 or after 2100")
  void getDailyStats_invalidYear_throwsSystemException() {
    assertThatThrownBy(
            () ->
                systemManagementService.getDailyStats(
                    LocalDate.of(1969, 12, 31), 7, List.of(ActionType.LOGIN)))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));

    assertThatThrownBy(
            () ->
                systemManagementService.getDailyStats(
                    LocalDate.of(2101, 1, 1), 7, List.of(ActionType.LOGIN)))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));
  }

  @Test
  @DisplayName("getDailyStats rejects days less than 1 or exceeding maxQueryLengthDays")
  void getDailyStats_invalidDays_throwsSystemException() {
    when(lokiProperties.getMaxQueryLengthDays()).thenReturn(30);

    assertThatThrownBy(
            () ->
                systemManagementService.getDailyStats(
                    LocalDate.of(2026, 7, 20), 0, List.of(ActionType.LOGIN)))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));

    assertThatThrownBy(
            () ->
                systemManagementService.getDailyStats(
                    LocalDate.of(2026, 7, 20), 31, List.of(ActionType.LOGIN)))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));
  }

  @Test
  @DisplayName("getDailyStats rejects null or empty actions")
  void getDailyStats_emptyOrNullActions_throwsSystemException() {
    when(lokiProperties.getMaxQueryLengthDays()).thenReturn(30);

    assertThatThrownBy(() -> systemManagementService.getDailyStats(LocalDate.of(2026, 7, 20), 7, null))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));

    assertThatThrownBy(
            () ->
                systemManagementService.getDailyStats(
                    LocalDate.of(2026, 7, 20), 7, Collections.emptyList()))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));
  }

  @Test
  @DisplayName("getDailyStats deduplicates and filters actions; falls back to defaults when all null")
  void getDailyStats_filtersActions() {
    when(lokiProperties.getMaxQueryLengthDays()).thenReturn(30);
    when(lokiQueryService.queryActivityMetricRange(anyString(), anyLong(), anyLong(), anyString()))
        .thenReturn(new LokiQueryRangeResponse("success", new LokiData("matrix", List.of(), null)));

    List<ActionType> actionsWithDuplicates =
        Arrays.asList(ActionType.LOGIN, ActionType.LOGIN, null);
    DailyStatsResponse resp1 =
        systemManagementService.getDailyStats(
            LocalDate.of(2026, 7, 20), 7, actionsWithDuplicates);
    assertThat(resp1.items().get(0).actionCounts()).containsKey("LOGIN");
    verify(lokiQueryService)
        .queryActivityMetricRange(eq("LOGIN"), anyLong(), anyLong(), eq("1d"));

    List<ActionType> allNullActions = Collections.singletonList(null);
    DailyStatsResponse resp2 =
        systemManagementService.getDailyStats(LocalDate.of(2026, 7, 20), 7, allNullActions);
    assertThat(resp2.items().get(0).actionCounts())
        .containsKeys("LOGIN", "REGISTER", "REQUEST_VIP");
  }

  @Test
  @DisplayName("getDailyStats parses diverse timestamps, values, and stream labels correctly")
  void getDailyStats_parsesLokiResults() {
    when(lokiProperties.getMaxQueryLengthDays()).thenReturn(30);

    LocalDate endDate = LocalDate.of(2026, 7, 20);
    LocalDate d1 = endDate.minusDays(2);
    LocalDate d2 = endDate.minusDays(1);
    LocalDate d3 = endDate;

    long d1Nano = d1.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() * 1_000_000L;
    long d2Milli = d2.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    long d3Sec = d3.atStartOfDay(ZoneOffset.UTC).toInstant().getEpochSecond();
    String d3SecStr = String.valueOf(d3Sec);

    LokiResultItem itemMetric =
        new LokiResultItem(
            Map.of("action", "LOGIN"),
            null,
            List.of(
                List.of(d1Nano, "5"),
                List.of(d2Milli, 12.0),
                List.of(d3Sec, 7),
                List.of(d3SecStr, "3"),
                List.of(d3Sec, "invalid-num")));

    LokiResultItem itemStream =
        new LokiResultItem(
            null,
            Map.of("action", "REGISTER"),
            List.of(
                List.of(d1Nano, 2),
                List.of("invalid-epoch", 99),
                Arrays.asList(d2Milli, null)));

    LokiResultItem itemNullAction = new LokiResultItem(Map.of(), null, List.of());
    LokiResultItem itemNullValues = new LokiResultItem(Map.of("action", "LOGIN"), null, null);

    LokiQueryRangeResponse lokiResponse =
        new LokiQueryRangeResponse(
            "success",
            new LokiData(
                "matrix",
                List.of(itemMetric, itemStream, itemNullAction, itemNullValues),
                null));

    when(lokiQueryService.queryActivityMetricRange(anyString(), anyLong(), anyLong(), anyString()))
        .thenReturn(lokiResponse);

    DailyStatsResponse response =
        systemManagementService.getDailyStats(
            endDate, 3, List.of(ActionType.LOGIN, ActionType.REGISTER));

    assertThat(response.totalDays()).isEqualTo(3);
    assertThat(response.startDate()).isEqualTo(d1);
    assertThat(response.endDate()).isEqualTo(endDate);
    assertThat(response.items()).hasSize(3);

    assertThat(response.items().get(0).date()).isEqualTo(d1);
    assertThat(response.items().get(0).actionCounts().get("LOGIN")).isEqualTo(5);
    assertThat(response.items().get(0).actionCounts().get("REGISTER")).isEqualTo(2);

    assertThat(response.items().get(1).date()).isEqualTo(d2);
    assertThat(response.items().get(1).actionCounts().get("LOGIN")).isEqualTo(12);
    assertThat(response.items().get(1).actionCounts().get("REGISTER")).isEqualTo(0);

    assertThat(response.items().get(2).date()).isEqualTo(d3);
    assertThat(response.items().get(2).actionCounts().get("LOGIN")).isEqualTo(10);
    assertThat(response.items().get(2).actionCounts().get("REGISTER")).isEqualTo(0);
  }

  @Test
  @DisplayName("getDailyStats handles null response from Loki without error")
  void getDailyStats_nullLokiResponse_returnsZeroes() {
    when(lokiProperties.getMaxQueryLengthDays()).thenReturn(30);
    when(lokiQueryService.queryActivityMetricRange(anyString(), anyLong(), anyLong(), anyString()))
        .thenReturn(null);

    DailyStatsResponse response =
        systemManagementService.getDailyStats(
            LocalDate.of(2026, 7, 20), 2, List.of(ActionType.LOGIN));

    assertThat(response.items()).hasSize(2);
    assertThat(response.items().get(0).actionCounts().get("LOGIN")).isEqualTo(0);
    assertThat(response.items().get(1).actionCounts().get("LOGIN")).isEqualTo(0);
  }

  @Test
  @DisplayName("getMonthlyStats rejects year out of range 1970-2100 or empty actions")
  void getMonthlyStats_invalidParams_throwsSystemException() {
    assertThatThrownBy(
            () -> systemManagementService.getMonthlyStats(1969, List.of(ActionType.LOGIN)))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));

    assertThatThrownBy(
            () -> systemManagementService.getMonthlyStats(2101, List.of(ActionType.LOGIN)))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));

    assertThatThrownBy(() -> systemManagementService.getMonthlyStats(2026, Collections.emptyList()))
        .isInstanceOf(SystemException.class)
        .satisfies(
            ex ->
                assertThat(((SystemException) ex).getCode())
                    .isEqualTo(SystemErrorCode.INVALID_PARAMETER.code()));
  }

  @Test
  @DisplayName("getMonthlyStats queries all 12 months and aggregates counts correctly")
  void getMonthlyStats_success_returns12Months() {
    LokiResultItem resultItem =
        new LokiResultItem(
            Map.of("action", "LOGIN"),
            null,
            List.of(List.of(123456789L, "15"), List.of(123456790L, 5)));

    LokiQueryRangeResponse lokiResponse =
        new LokiQueryRangeResponse(
            "success", new LokiData("matrix", List.of(resultItem), null));

    when(lokiQueryService.queryActivityMetricRange(eq("LOGIN"), anyLong(), anyLong(), eq("1d")))
        .thenReturn(lokiResponse);

    MonthlyStatsResponse response =
        systemManagementService.getMonthlyStats(2026, List.of(ActionType.LOGIN));

    assertThat(response.year()).isEqualTo(2026);
    assertThat(response.items()).hasSize(12);

    for (int month = 1; month <= 12; month++) {
      assertThat(response.items().get(month - 1).month()).isEqualTo(month);
      assertThat(response.items().get(month - 1).year()).isEqualTo(2026);
      assertThat(response.items().get(month - 1).actionCounts().get("LOGIN")).isEqualTo(20);
    }
  }
}
