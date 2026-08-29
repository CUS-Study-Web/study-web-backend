package studyweb.cus.mapper.badge;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.entity.badge.Badge;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BadgeMapper {

  @Mapping(source = "badge.createdBy.id", target = "createdBy")
  BadgeResponse toResponse(Badge badge);
}
