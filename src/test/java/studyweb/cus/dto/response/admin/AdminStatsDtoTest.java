package studyweb.cus.dto.response.admin;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import studyweb.cus.dto.request.user.VipSubscriptionRequest;
import studyweb.cus.dto.response.admin.LokiQueryRangeResponse.LokiData;
import studyweb.cus.dto.response.admin.LokiQueryRangeResponse.LokiResultItem;
import studyweb.cus.enums.ActionType;

class AdminStatsDtoTest {

  @Test
  @DisplayName("VipSubscriptionRequest holds note correctly")
  void vipSubscriptionRequest_holdsNote() {
    VipSubscriptionRequest request = new VipSubscriptionRequest("Renewal request");
    assertThat(request.note()).isEqualTo("Renewal request");
  }

  @Test
  @DisplayName("DailyStatsResponse and DailyStatItemResponse hold properties correctly")
  void dailyStatsResponses_holdProperties() {
    LocalDate date = LocalDate.of(2026, 7, 20);
    DailyStatItemResponse item = new DailyStatItemResponse(date, Map.of("LOGIN", 5));
    assertThat(item.date()).isEqualTo(date);
    assertThat(item.actionCounts()).containsEntry("LOGIN", 5);

    DailyStatsResponse response =
        new DailyStatsResponse(date.minusDays(1), date, 2, java.util.List.of(item));
    assertThat(response.startDate()).isEqualTo(date.minusDays(1));
    assertThat(response.endDate()).isEqualTo(date);
    assertThat(response.totalDays()).isEqualTo(2);
    assertThat(response.items()).containsExactly(item);
  }

  @Test
  @DisplayName("MonthlyStatsResponse and MonthlyStatItemResponse hold properties correctly")
  void monthlyStatsResponses_holdProperties() {
    MonthlyStatItemResponse item = new MonthlyStatItemResponse(7, 2026, Map.of("REGISTER", 2));
    assertThat(item.month()).isEqualTo(7);
    assertThat(item.year()).isEqualTo(2026);
    assertThat(item.actionCounts()).containsEntry("REGISTER", 2);

    MonthlyStatsResponse response = new MonthlyStatsResponse(2026, java.util.List.of(item));
    assertThat(response.year()).isEqualTo(2026);
    assertThat(response.items()).containsExactly(item);
  }

  @Test
  @DisplayName("LokiQueryRangeResponse records hold properties correctly")
  void lokiQueryRangeResponse_holdsProperties() {
    LokiResultItem item =
        new LokiResultItem(Map.of("action", "LOGIN"), Map.of("stream", "val"), java.util.List.of());
    assertThat(item.metric()).containsEntry("action", "LOGIN");
    assertThat(item.stream()).containsEntry("stream", "val");
    assertThat(item.values()).isEmpty();

    LokiData data = new LokiData("matrix", java.util.List.of(item), "stats");
    assertThat(data.resultType()).isEqualTo("matrix");
    assertThat(data.result()).containsExactly(item);
    assertThat(data.stats()).isEqualTo("stats");

    LokiQueryRangeResponse response = new LokiQueryRangeResponse("success", data);
    assertThat(response.status()).isEqualTo("success");
    assertThat(response.data()).isEqualTo(data);
  }

  @Test
  @DisplayName("ActionType contains all required activity log action types")
  void actionType_containsExpectedValues() {
    assertThat(ActionType.valueOf("LOGIN")).isNotNull();
    assertThat(ActionType.valueOf("LOGOUT")).isNotNull();
    assertThat(ActionType.valueOf("REGISTER")).isNotNull();
    assertThat(ActionType.valueOf("SUBMIT_ASSESSMENT")).isNotNull();
    assertThat(ActionType.valueOf("REQUEST_VIP")).isNotNull();
    assertThat(ActionType.valueOf("CREATE_LESSON")).isNotNull();
    assertThat(ActionType.valueOf("UPDATE_LESSON")).isNotNull();
    assertThat(ActionType.valueOf("DELETE_LESSON")).isNotNull();
    assertThat(ActionType.valueOf("CREATE_ASSESSMENT")).isNotNull();
    assertThat(ActionType.valueOf("UPDATE_ASSESSMENT")).isNotNull();
    assertThat(ActionType.valueOf("DELETE_ASSESSMENT")).isNotNull();
  }
}
