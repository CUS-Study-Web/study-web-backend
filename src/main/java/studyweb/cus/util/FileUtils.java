package studyweb.cus.util;

import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;
import studyweb.cus.enums.DocumentFileType;

public final class FileUtils {

  private FileUtils() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Extracts the file extension (lowercase without dot) from a filename.
   * Returns {@code null} if filename is null or contains no extension.
   *
   * @param filename the name of the file
   * @return lowercase extension or {@code null}
   */
  public static String getExtension(String filename) {
    if (filename == null) {
      return null;
    }
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex < 0 || dotIndex == filename.length() - 1) {
      return null;
    }
    return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
  }

  /**
   * Extracts the file extension (lowercase without dot) from a {@link MultipartFile}.
   * Returns {@code null} if file or originalFilename is null or contains no extension.
   *
   * @param file the uploaded file
   * @return lowercase extension or {@code null}
   */
  public static String getExtension(MultipartFile file) {
    if (file == null) {
      return null;
    }
    return getExtension(file.getOriginalFilename());
  }

  /**
   * Resolves the {@link DocumentFileType} from a {@link MultipartFile} and an optional explicit type override.
   * If explicitType is non-null, it is returned.
   * If file or filename is null, defaults to {@link DocumentFileType#PDF}.
   * Otherwise resolves from the filename extension.
   *
   * @param file         the uploaded file
   * @param explicitType optional explicit type override
   * @return resolved {@link DocumentFileType}
   */
  public static DocumentFileType resolveDocumentFileType(
      MultipartFile file, DocumentFileType explicitType) {
    if (explicitType != null) {
      return explicitType;
    }
    if (file == null || file.getOriginalFilename() == null) {
      return DocumentFileType.PDF;
    }
    return resolveDocumentFileType(file.getOriginalFilename());
  }


  /**
   * Resolves the {@link DocumentFileType} from a filename and an optional explicit type override.
   *
   * @param filename     the file name
   * @param explicitType optional explicit type override
   * @return resolved {@link DocumentFileType}
   */
  public static DocumentFileType resolveDocumentFileType(
      String filename, DocumentFileType explicitType) {
    if (explicitType != null) {
      return explicitType;
    }
    return resolveDocumentFileType(filename);
  }

  /**
   * Resolves the {@link DocumentFileType} from a filename by checking its extension.
   * Defaults to {@link DocumentFileType#PDF} if unrecognized or filename is null.
   *
   * @param filename the file name
   * @return resolved {@link DocumentFileType}
   */
  public static DocumentFileType resolveDocumentFileType(String filename) {
    if (filename == null) {
      return DocumentFileType.PDF;
    }
    String lower = filename.toLowerCase(Locale.ROOT);
    if (lower.endsWith(".docx")) return DocumentFileType.DOCX;
    if (lower.endsWith(".doc")) return DocumentFileType.DOC;
    if (lower.endsWith(".xlsx")) return DocumentFileType.XLSX;
    if (lower.endsWith(".xls")) return DocumentFileType.XLS;
    if (lower.endsWith(".pptx")) return DocumentFileType.PPTX;
    return DocumentFileType.PDF;
  }
}
