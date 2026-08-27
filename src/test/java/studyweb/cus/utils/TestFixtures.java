package studyweb.cus.utils;

import java.util.UUID;
import studyweb.cus.entity.course.Course;
import studyweb.cus.entity.course.Subject;
import studyweb.cus.entity.course.Assessment;
import studyweb.cus.entity.user.User;
import studyweb.cus.enums.AssessmentType;
import studyweb.cus.enums.AssessmentFileType;
import studyweb.cus.enums.AssessmentStatus;
import studyweb.cus.enums.AccessTier;

public class TestFixtures {

    public static Course createMockCourse(UUID courseId) {
        Course c = new Course();
        c.setId(courseId);
        c.setTitle("Java Course");
        return c;
    }

    public static Subject createMockSubject(UUID subjectId, Course course) {
        Subject s = new Subject();
        s.setId(subjectId);
        s.setCourse(course);
        s.setTitle("Basics");
        return s;
    }

    public static User createMockUser(UUID userId, String email) {
        User u = new User();
        u.setId(userId);
        u.setGmail(email);
        return u;
    }

    public static Assessment createMockExam(UUID assessmentId, Course course) {
        Assessment a = new Assessment();
        a.setId(assessmentId);
        a.setTitle("Midterm Exam");
        a.setAssessmentType(AssessmentType.EXAM);
        a.setNumQuestions(40);
        a.setMaxScore(100);
        a.setStatus(AssessmentStatus.DRAFT);
        a.setCourse(course);
        a.setAccess(AccessTier.PUBLIC);
        a.setFileType(AssessmentFileType.PDF);
        a.setFileKey("exams/exam.pdf");
        return a;
    }

    public static Assessment createMockExam(UUID assessmentId, Course course, int numQuestions, int maxScore) {
        Assessment a = createMockExam(assessmentId, course);
        a.setNumQuestions(numQuestions);
        a.setMaxScore(maxScore);
        return a;
    }

    public static Assessment createMockHomework(UUID assessmentId, Subject subject) {
        Assessment a = new Assessment();
        a.setId(assessmentId);
        a.setTitle("Homework 1");
        a.setAssessmentType(AssessmentType.HOMEWORK);
        a.setNumQuestions(10);
        a.setSubject(subject);
        a.setAccess(AccessTier.PUBLIC);
        a.setFileType(AssessmentFileType.PDF);
        a.setFileKey("exercises/hw.pdf");
        return a;
    }
}
