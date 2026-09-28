package studyweb.cus.mapper.course;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import studyweb.cus.dto.request.course.AchievementScoreRequest;
import studyweb.cus.dto.response.course.AchievementScoreResponse;
import studyweb.cus.dto.request.course.LeaderboardRequest;
import studyweb.cus.dto.response.course.LeaderboardResponse;
import studyweb.cus.entity.course.AchievementScore;
import studyweb.cus.entity.course.Leaderboard;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LeaderboardMapper {

    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "course.title", target = "courseName")
    LeaderboardResponse toLeaderboardResponse(Leaderboard leaderboard);

    @Mapping(source = "examSubject.id", target = "subjectId")
    @Mapping(source = "examSubject.title", target = "subjectName")
    AchievementScoreResponse toAchievementScoreResponse(AchievementScore achievementScore);

    @Mapping(target = "course", ignore = true)
    Leaderboard toLeaderboard(LeaderboardRequest dto);

    @Mapping(target = "examSubject", ignore = true)
    @Mapping(target = "achievement", ignore = true)
    AchievementScore toAchievementScore(AchievementScoreRequest dto);
}
