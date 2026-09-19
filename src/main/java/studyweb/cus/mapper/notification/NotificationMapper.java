package studyweb.cus.mapper.notification;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.notification.NotificationResponse;
import studyweb.cus.entity.user.Notification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface NotificationMapper {

  @Mapping(source = "read", target = "isRead")
  NotificationResponse toResponse(Notification notification);
}
