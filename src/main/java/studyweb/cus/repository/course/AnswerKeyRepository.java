package studyweb.cus.repository.course;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import studyweb.cus.entity.course.AnswerKey;

public interface AnswerKeyRepository extends JpaRepository<AnswerKey, UUID> {

  List<AnswerKey> findByExamIdAndDeletedAtIsNullOrderByQuestionNumberAsc(UUID examId);

  List<AnswerKey> findByExamIdInAndDeletedAtIsNull(List<UUID> examIds);

  void deleteByExamId(UUID examId);
}
