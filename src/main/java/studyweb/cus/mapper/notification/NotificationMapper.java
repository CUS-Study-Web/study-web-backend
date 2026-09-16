package studyweb.cus.mapper.notification;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.notification.NotificationResponse;
import studyweb.cus.entity.user.Notification;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

  NotificationResponse toResponse(Notification notification);
}
