package studyweb.cus.service.admin.impl;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.admin.PricingPageUpdateRequest;
import studyweb.cus.dto.request.content.VipFeatureRequest;
import studyweb.cus.dto.response.content.PricingPageResponse;
import studyweb.cus.dto.response.content.VipFeatureResponse;
import studyweb.cus.entity.content.PricingPageContent;
import studyweb.cus.entity.content.VipFeature;
import studyweb.cus.exception.system.SystemErrorCode;
import studyweb.cus.exception.system.SystemException;
import studyweb.cus.mapper.admin.PricingPageMapper;
import studyweb.cus.mapper.admin.VipFeatureMapper;
import studyweb.cus.repository.content.PricingPageContentRepository;
import studyweb.cus.repository.content.VipFeatureRepository;
import studyweb.cus.service.admin.PricingPageService;

@Service
@RequiredArgsConstructor
public class PricingPageServiceImpl implements PricingPageService {

    private final PricingPageContentRepository pricingPageContentRepository;
    private final VipFeatureRepository vipFeatureRepository;
    private final PricingPageMapper pricingPageMapper;
    private final VipFeatureMapper vipFeatureMapper;

    @Override
    @Transactional(readOnly = true)
    public PricingPageResponse getPricingPage() {
        PricingPageContent setting = getLatestPricingPageContent();
        List<VipFeature> features = vipFeatureRepository.findBySettingOrderByCreatedAtAsc(setting);
        List<VipFeatureResponse> featureResponses = features.stream()
                .map(vipFeatureMapper::toVipFeatureResponse)
                .toList();
        return pricingPageMapper.toPricingPageResponse(setting, featureResponses);
    }

    @Override
    @Transactional
    public PricingPageResponse updatePricingPage(PricingPageUpdateRequest request) {
        PricingPageContent setting = getLatestPricingPageContent();

        if (request.normalPackage() != null) {
            if (request.normalPackage().name() != null && !request.normalPackage().name().isBlank())
                setting.setNormalPkgName(request.normalPackage().name());
            if (request.normalPackage().price() != null && !request.normalPackage().price().isBlank())
                setting.setNormalPkgPrice(request.normalPackage().price());
            if (request.normalPackage().description() != null && !request.normalPackage().description().isBlank())
                setting.setNormalPkgDesc(request.normalPackage().description());
            if (request.normalPackage().buttonText() != null && !request.normalPackage().buttonText().isBlank())
                setting.setNormalBtnText(request.normalPackage().buttonText());
        }

        if (request.vipPackage() != null) {
            if (request.vipPackage().name() != null && !request.vipPackage().name().isBlank())
                setting.setVipPkgName(request.vipPackage().name());
            if (request.vipPackage().price() != null && !request.vipPackage().price().isBlank())
                setting.setVipPkgPrice(request.vipPackage().price());
            if (request.vipPackage().billingPeriod() != null && !request.vipPackage().billingPeriod().isBlank())
                setting.setVipPkgBillingPeriod(request.vipPackage().billingPeriod());
            if (request.vipPackage().description() != null && !request.vipPackage().description().isBlank())
                setting.setVipPkgDesc(request.vipPackage().description());
            if (request.vipPackage().buttonText() != null && !request.vipPackage().buttonText().isBlank())
                setting.setVipBtnText(request.vipPackage().buttonText());
            if (request.vipPackage().tag() != null && !request.vipPackage().tag().isBlank())
                setting.setVipPkgTag(request.vipPackage().tag());
        }

        pricingPageContentRepository.save(setting);

        List<VipFeature> features = vipFeatureRepository.findBySettingOrderByCreatedAtAsc(setting);
        List<VipFeatureResponse> featureResponses = features.stream()
                .map(vipFeatureMapper::toVipFeatureResponse)
                .toList();
        return pricingPageMapper.toPricingPageResponse(setting, featureResponses);
    }

    @Override
    @Transactional
    public VipFeatureResponse addFeature(VipFeatureRequest request) {
        PricingPageContent setting = getLatestPricingPageContent();
        VipFeature feature = vipFeatureMapper.toVipFeature(request);
        feature.setSetting(setting);
        feature = vipFeatureRepository.save(feature);
        return vipFeatureMapper.toVipFeatureResponse(feature);
    }

    @Override
    @Transactional
    public VipFeatureResponse updateFeature(UUID id, VipFeatureRequest request) {
        VipFeature feature = vipFeatureRepository.findById(id)
                .orElseThrow(() -> new SystemException(SystemErrorCode.RESOURCE_NOT_FOUND, "VipFeature not found"));

        if (request.featureName() != null && !request.featureName().isBlank())
            feature.setFeatureName(request.featureName());
        if (request.iconNormalAccess() != null)
            feature.setIconNormalAccess(request.iconNormalAccess());
        if (request.normalAccess() != null && !request.normalAccess().isBlank())
            feature.setNormalAccess(request.normalAccess());
        if (request.iconVipAccess() != null)
            feature.setIconVipAccess(request.iconVipAccess());
        if (request.vipAccess() != null && !request.vipAccess().isBlank())
            feature.setVipAccess(request.vipAccess());
        if (request.normalHasIcon() != null)
            feature.setNormalHasIcon(request.normalHasIcon());
        if (request.vipHasIcon() != null)
            feature.setVipHasIcon(request.vipHasIcon());

        feature = vipFeatureRepository.save(feature);
        return vipFeatureMapper.toVipFeatureResponse(feature);
    }

    @Override
    @Transactional
    public void deleteFeature(UUID id) {
        if (!vipFeatureRepository.existsById(id)) {
            throw new SystemException(SystemErrorCode.RESOURCE_NOT_FOUND, "VipFeature not found");
        }
        vipFeatureRepository.deleteById(id);
    }

    private PricingPageContent getLatestPricingPageContent() {
        return pricingPageContentRepository.findFirstByOrderByCreatedAtDesc()
                .orElseThrow(() -> new SystemException(SystemErrorCode.RESOURCE_NOT_FOUND,
                        "Pricing page content not found"));
    }
}
