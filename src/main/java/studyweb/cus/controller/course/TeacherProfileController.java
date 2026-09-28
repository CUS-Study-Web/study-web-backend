package studyweb.cus.controller.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import studyweb.cus.controller.AbstractBaseController;
import studyweb.cus.dto.base.PageResponse;
import studyweb.cus.dto.base.SingleResponse;
import studyweb.cus.dto.base.SuccessResponse;
import studyweb.cus.dto.request.course.TeacherProfileRequest;
import studyweb.cus.dto.response.course.TeacherProfileResponse;
import studyweb.cus.service.course.TeacherProfileService;

@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Teachers", description = "Teacher Profile Management")
public class TeacherProfileController extends AbstractBaseController {

  private final TeacherProfileService teacherProfileService;

  @GetMapping("/guest")
  @Operation(summary = "Get all teachers", description = "Get all teachers")
  public ResponseEntity<PageResponse<TeacherProfileResponse>> getTeachers(Pageable pageable) {
    log.info("[GET /api/teachers/guest] Get all teachers guest, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return paging(teacherProfileService.getTeachers(pageable), "Teachers fetched successfully!");
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Get all teachers for admin", description = "Get all teachers for admin")
  public ResponseEntity<PageResponse<TeacherProfileResponse>> getTeachersAdmin(Pageable pageable) {
    log.info("[GET /api/teachers] Get all teachers admin, page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
    return paging(teacherProfileService.getTeachers(pageable), "Teachers fetched successfully!");
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Create a teacher profile", description = "Create a teacher profile")
  public ResponseEntity<SingleResponse<TeacherProfileResponse>> createTeacher(
      @Valid @ModelAttribute TeacherProfileRequest request) {
    log.info("[POST /api/teachers] Create teacher profile for name: {}", request.name());
    return successSingle(teacherProfileService.createTeacher(request), "Teacher created successfully!");
  }

  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update a teacher profile", description = "Update a teacher profile")
  public ResponseEntity<SingleResponse<TeacherProfileResponse>> updateTeacher(
      @PathVariable UUID id, @ModelAttribute TeacherProfileRequest request) {
    log.info("[PATCH /api/teachers/{}] Update teacher profile id: {}", id, id);
    return successSingle(teacherProfileService.updateTeacher(id, request), "Teacher updated successfully!");
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Delete a teacher profile", description = "Delete a teacher profile")
  public ResponseEntity<SuccessResponse> deleteTeacher(@PathVariable UUID id) {
    log.info("[DELETE /api/teachers/{}] Delete teacher profile id: {}", id, id);
    teacherProfileService.deleteTeacher(id);
    return success("Teacher deleted successfully!");
  }
}
