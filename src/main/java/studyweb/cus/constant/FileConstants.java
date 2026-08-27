package studyweb.cus.constant;

import java.util.List;
import java.util.Set;

public final class FileConstants {

  private FileConstants() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  public static final String FOLDER_DOCUMENTS = "documents/";
  public static final String FOLDER_AVATARS = "avatars/";
  public static final String FOLDER_EXERCISES = "exercises/";
  public static final String FOLDER_EXAMS = "exams/";

  public static final List<String> ALL_FOLDERS =
      List.of(FOLDER_DOCUMENTS, FOLDER_AVATARS, FOLDER_EXERCISES, FOLDER_EXAMS);

  public static final Set<String> AVATAR_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
  public static final Set<String> DOCUMENT_EXTENSIONS =
      Set.of("pdf", "doc", "docx", "xls", "xlsx", "pptx", "ppt");
  public static final Set<String> EXERCISE_EXTENSIONS = DOCUMENT_EXTENSIONS;
  public static final Set<String> EXAM_EXTENSIONS = DOCUMENT_EXTENSIONS;
}
