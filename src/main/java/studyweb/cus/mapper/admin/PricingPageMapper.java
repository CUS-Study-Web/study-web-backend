package studyweb.cus.mapper.admin;

import java.util.List;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.request.admin.PricingPageUpdateRequest;
import studyweb.cus.dto.response.content.PricingPageResponse;
import studyweb.cus.dto.response.content.VipFeatureResponse;
import studyweb.cus.entity.content.PricingPageContent;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PricingPageMapper {

    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "normalPackage.name", source = "entity.normalPkgName")
    @Mapping(target = "normalPackage.price", source = "entity.normalPkgPrice")
    @Mapping(target = "normalPackage.description", source = "entity.normalPkgDesc")
    @Mapping(target = "normalPackage.buttonText", source = "entity.normalBtnText")
    @Mapping(target = "vipPackage.tag", source = "entity.vipPkgTag")
    @Mapping(target = "vipPackage.name", source = "entity.vipPkgName")
    @Mapping(target = "vipPackage.price", source = "entity.vipPkgPrice")
    @Mapping(target = "vipPackage.billingPeriod", source = "entity.vipPkgBillingPeriod")
    @Mapping(target = "vipPackage.description", source = "entity.vipPkgDesc")
    @Mapping(target = "vipPackage.buttonText", source = "entity.vipBtnText")
    @Mapping(target = "features", source = "features")
    PricingPageResponse toPricingPageResponse(PricingPageContent entity, List<VipFeatureResponse> features);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "normalPkgName", source = "request.normalPackage.name")
    @Mapping(target = "normalPkgPrice", source = "request.normalPackage.price")
    @Mapping(target = "normalPkgDesc", source = "request.normalPackage.description")
    @Mapping(target = "normalBtnText", source = "request.normalPackage.buttonText")
    @Mapping(target = "vipPkgTag", source = "request.vipPackage.tag")
    @Mapping(target = "vipPkgName", source = "request.vipPackage.name")
    @Mapping(target = "vipPkgPrice", source = "request.vipPackage.price")
    @Mapping(target = "vipPkgBillingPeriod", source = "request.vipPackage.billingPeriod")
    @Mapping(target = "vipPkgDesc", source = "request.vipPackage.description")
    @Mapping(target = "vipBtnText", source = "request.vipPackage.buttonText")
    void updatePricingPageFromDto(PricingPageUpdateRequest request, @MappingTarget PricingPageContent entity);
}
