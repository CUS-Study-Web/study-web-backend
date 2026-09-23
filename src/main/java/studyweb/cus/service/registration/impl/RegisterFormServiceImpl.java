package studyweb.cus.service.registration.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;
import studyweb.cus.entity.registration.RegisterForm;
import studyweb.cus.mapper.registration.RegisterFormMapper;
import studyweb.cus.repository.registration.RegisterFormRepository;
import studyweb.cus.service.registration.RegisterFormService;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterFormServiceImpl implements RegisterFormService {

  private final RegisterFormRepository registerFormRepository;
  private final RegisterFormMapper registerFormMapper;

  @Override
  @Transactional
  public RegisterFormResponse createRegisterForm(RegisterFormRequest request) {
    log.info("Processing offline exam registration form for email: {}", request.email());
    RegisterForm entity = registerFormMapper.toEntity(request);
    RegisterForm saved = registerFormRepository.save(entity);
    log.info("Offline exam registration form saved successfully with id: {}", saved.getId());
    return registerFormMapper.toResponse(saved);
  }
}
