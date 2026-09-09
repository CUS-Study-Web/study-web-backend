package studyweb.cus.mapper.admin;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.request.content.VipFeatureRequest;
import studyweb.cus.dto.response.content.VipFeatureResponse;
import studyweb.cus.entity.content.VipFeature;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VipFeatureMapper {

    VipFeatureResponse toVipFeatureResponse(VipFeature entity);

    VipFeature toVipFeature(VipFeatureRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateVipFeatureFromDto(VipFeatureRequest request, @MappingTarget VipFeature entity);
}
