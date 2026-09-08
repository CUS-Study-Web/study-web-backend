package studyweb.cus.service.course.impl;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studyweb.cus.dto.request.course.TeacherProfileRequest;
import studyweb.cus.dto.response.course.TeacherProfileResponse;
import studyweb.cus.entity.course.TeacherProfile;
import studyweb.cus.mapper.course.TeacherProfileMapper;
import studyweb.cus.repository.course.TeacherProfileRepository;
import studyweb.cus.service.course.TeacherProfileService;

import studyweb.cus.service.file.FileService;
import studyweb.cus.dto.response.document.UploadDocumentResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherProfileServiceImpl implements TeacherProfileService {

    private final TeacherProfileRepository teacherProfileRepository;
    private final TeacherProfileMapper teacherProfileMapper;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public Page<TeacherProfileResponse> getTeachers(Pageable pageable) {
        log.info("Fetching all teacher profiles");
        return teacherProfileRepository.findAll(pageable)
                .map(teacherProfileMapper::toTeacherProfileResponse);
    }

    @Override
    @Transactional
    public TeacherProfileResponse createTeacher(TeacherProfileRequest request) {
        log.info("Creating a new teacher profile for name: {}", request.name());
        TeacherProfile entity = teacherProfileMapper.toTeacherProfile(request);
        if (entity.getSubject() != null) {
            entity.setSubject(entity.getSubject().toUpperCase().trim());
        }
        if (request.avatarImage() != null && !request.avatarImage().isEmpty()) {
            log.info("Uploading avatar image for new teacher profile");
            UploadDocumentResult uploadResult = fileService.uploadAvatarFile(request.avatarImage());
            entity.setAvatarUrl(uploadResult.fileUrl());
        }
        TeacherProfile savedEntity = teacherProfileRepository.save(entity);
        log.info("Successfully created teacher profile with ID: {}", savedEntity.getId());
        return teacherProfileMapper.toTeacherProfileResponse(savedEntity);
    }

    @Override
    @Transactional
    public TeacherProfileResponse updateTeacher(UUID id, TeacherProfileRequest request) {
        log.info("Updating teacher profile with ID: {}", id);
        TeacherProfile entity = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher Profile not found"));

        if (request.name() != null && !request.name().isBlank()) {
            entity.setName(request.name());
        }
        if (request.description() != null && !request.description().isBlank()) {
            entity.setDescription(request.description());
        }

        if (request.avatarImage() != null && !request.avatarImage().isEmpty()) {
            log.info("Uploading new avatar image for teacher profile ID: {}", id);
            UploadDocumentResult uploadResult = fileService.uploadAvatarFile(request.avatarImage());
            entity.setAvatarUrl(uploadResult.fileUrl());
        }

        if (request.subject() != null && !request.subject().isBlank()) {
            entity.setSubject(request.subject().toUpperCase().trim());
        }

        TeacherProfile savedEntity = teacherProfileRepository.save(entity);
        log.info("Successfully updated teacher profile with ID: {}", id);
        return teacherProfileMapper.toTeacherProfileResponse(savedEntity);
    }

    @Override
    @Transactional
    public void deleteTeacher(UUID id) {
        log.info("Deleting teacher profile with ID: {}", id);
        TeacherProfile entity = teacherProfileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher Profile not found"));
        teacherProfileRepository.delete(entity);
        log.info("Successfully deleted teacher profile with ID: {}", id);
    }
}
