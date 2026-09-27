package studyweb.cus.mapper.content;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.request.content.RequestVipFormContentRequest;
import studyweb.cus.dto.response.content.RequestVipFormContentResponse;
import studyweb.cus.entity.content.RequestVipFormContent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RequestVipFormContentMapper {

  @Mapping(source = "updatedBy.id", target = "updatedBy")
  RequestVipFormContentResponse toResponse(RequestVipFormContent entity);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateEntityFromRequest(
      RequestVipFormContentRequest request, @MappingTarget RequestVipFormContent entity);
}
