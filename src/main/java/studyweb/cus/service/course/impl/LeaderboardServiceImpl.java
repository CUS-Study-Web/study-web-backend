package studyweb.cus.service.course.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.dto.request.course.AchievementScoreRequest;
import studyweb.cus.dto.request.course.LeaderboardRequest;
import studyweb.cus.dto.response.course.AchievementScoreResponse;
import studyweb.cus.dto.response.course.LeaderboardResponse;
import studyweb.cus.dto.response.document.UploadDocumentResult;
import studyweb.cus.entity.course.AchievementScore;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Leaderboard;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.exception.course.CourseErrorCode;
import studyweb.cus.exception.course.CourseException;
import studyweb.cus.mapper.course.LeaderboardMapper;
import studyweb.cus.repository.course.AchievementScoreRepository;
import studyweb.cus.repository.course.CourseRepository;
import studyweb.cus.repository.course.LeaderboardRepository;
import studyweb.cus.repository.course.SubjectRepository;
import studyweb.cus.service.course.LeaderboardService;
import studyweb.cus.service.file.FileService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final AchievementScoreRepository achievementScoreRepository;
    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final LeaderboardMapper leaderboardMapper;
    private final FileService fileService;

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardResponse> getLeaderboards(Pageable pageable) {
        log.info("Fetching all leaderboards");
        return leaderboardRepository.findAll(pageable)
                .map(leaderboardMapper::toLeaderboardResponse);
    }

    @Override
    @Transactional
    public LeaderboardResponse createLeaderboard(LeaderboardRequest request) {
        log.info("Creating a new leaderboard entry for course ID: {}", request.courseId());
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));

        Leaderboard leaderboard = leaderboardMapper.toLeaderboard(request);
        leaderboard.setCourse(course);

        if (request.achievement() != null) {
            leaderboard.setAchievement(request.achievement().trim());
        }

        if (request.avatarImage() != null && !request.avatarImage().isEmpty()) {
            log.info("Uploading avatar image for new leaderboard entry");
            UploadDocumentResult uploadResult = fileService.uploadAvatarFile(request.avatarImage());
            leaderboard.setAvatarUrl(uploadResult.fileUrl());
        }

        Leaderboard saved = leaderboardRepository.save(leaderboard);
        log.info("Successfully created leaderboard entry with ID: {}", saved.getId());
        return leaderboardMapper.toLeaderboardResponse(saved);
    }

    @Override
    @Transactional
    public LeaderboardResponse updateLeaderboard(UUID id, LeaderboardRequest request) {
        log.info("Updating leaderboard entry with ID: {}", id);
        Leaderboard leaderboard = leaderboardRepository.findById(id)
                .orElseThrow(() -> new CourseException(CourseErrorCode.LEADERBOARD_NOT_FOUND));

        if (request.courseId() != null) {
            log.info("Updating course reference for leaderboard ID: {}", id);
            Course course = courseRepository.findById(request.courseId())
                    .orElseThrow(() -> new CourseException(CourseErrorCode.COURSE_NOT_FOUND));
            leaderboard.setCourse(course);
        }

        if (request.studentName() != null && !request.studentName().isBlank()) {
            leaderboard.setStudentName(request.studentName());
        }

        if (request.sumScore() != null) {
            leaderboard.setSumScore(request.sumScore());
        }
        
        if (request.achievement() != null && !request.achievement().isBlank()) {
            leaderboard.setAchievement(request.achievement().trim());
        }

        if (request.avatarImage() != null && !request.avatarImage().isEmpty()) {
            log.info("Uploading new avatar image for leaderboard ID: {}", id);
            UploadDocumentResult uploadResult = fileService.uploadAvatarFile(request.avatarImage());
            leaderboard.setAvatarUrl(uploadResult.fileUrl());
        }

        Leaderboard saved = leaderboardRepository.save(leaderboard);
        log.info("Successfully updated leaderboard entry with ID: {}", id);
        return leaderboardMapper.toLeaderboardResponse(saved);
    }

    @Override
    @Transactional
    public void deleteLeaderboard(UUID id) {
        log.info("Deleting leaderboard entry with ID: {}", id);
        Leaderboard leaderboard = leaderboardRepository.findById(id)
                .orElseThrow(() -> new CourseException(CourseErrorCode.LEADERBOARD_NOT_FOUND));
        leaderboardRepository.delete(leaderboard);
        log.info("Successfully deleted leaderboard entry with ID: {}", id);
    }

    @Override
    @Transactional
    public AchievementScoreResponse addScore(UUID leaderboardId, AchievementScoreRequest request) {
        log.info("Adding new achievement score for leaderboard ID: {}, subject ID: {}", leaderboardId, request.subjectId());
        Leaderboard leaderboard = leaderboardRepository.findById(leaderboardId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.LEADERBOARD_NOT_FOUND));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new CourseException(CourseErrorCode.SUBJECT_NOT_FOUND));

        AchievementScore score = leaderboardMapper.toAchievementScore(request);
        score.setAchievement(leaderboard);
        score.setExamSubject(subject);
        
        AchievementScore saved = achievementScoreRepository.save(score);
        log.info("Successfully added achievement score with ID: {}", saved.getId());
        return leaderboardMapper.toAchievementScoreResponse(saved);
    }

    @Override
    @Transactional
    public AchievementScoreResponse updateScore(UUID scoreId, AchievementScoreRequest request) {
        log.info("Updating achievement score with ID: {}", scoreId);
        AchievementScore score = achievementScoreRepository.findById(scoreId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.SCORE_NOT_FOUND));

        if (request.subjectId() != null) {
            log.info("Updating subject reference for achievement score ID: {}", scoreId);
            Subject subject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new CourseException(CourseErrorCode.SUBJECT_NOT_FOUND));
            score.setExamSubject(subject);
        }

        if (request.score() != null) {
            score.setScore(request.score());
        }
        
        AchievementScore saved = achievementScoreRepository.save(score);
        log.info("Successfully updated achievement score with ID: {}", scoreId);
        return leaderboardMapper.toAchievementScoreResponse(saved);
    }

    @Override
    @Transactional
    public void deleteScore(UUID scoreId) {
        log.info("Deleting achievement score with ID: {}", scoreId);
        AchievementScore score = achievementScoreRepository.findById(scoreId)
                .orElseThrow(() -> new CourseException(CourseErrorCode.SCORE_NOT_FOUND));
        achievementScoreRepository.delete(score);
        log.info("Successfully deleted achievement score with ID: {}", scoreId);
    }
}
