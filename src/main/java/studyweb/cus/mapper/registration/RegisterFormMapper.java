package studyweb.cus.mapper.registration;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.request.registration.RegisterFormRequest;
import studyweb.cus.dto.response.registration.RegisterFormResponse;
import studyweb.cus.entity.registration.RegisterForm;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegisterFormMapper {

  @Mapping(
      target = "registeredDate",
      expression =
          "java(request.registeredDate() != null ? request.registeredDate() : java.time.LocalDate.now())")
  RegisterForm toEntity(RegisterFormRequest request);

  RegisterFormResponse toResponse(RegisterForm registerForm);
}
