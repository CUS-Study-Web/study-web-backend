package studyweb.cus.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import studyweb.cus.enums.DocumentFileType;

class FileUtilsTest {

  @Test
  @DisplayName("Private constructor throws UnsupportedOperationException")
  void privateConstructorThrowsException() throws Exception {
    Constructor<FileUtils> constructor = FileUtils.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertThatThrownBy(constructor::newInstance)
        .isInstanceOf(InvocationTargetException.class)
        .hasCauseInstanceOf(UnsupportedOperationException.class);
  }

  @Nested
  @DisplayName("getExtension Tests")
  class GetExtensionTests {

    @ParameterizedTest
    @CsvSource({
      "document.pdf, pdf",
      "my.report.docx, docx",
      "SHEET.XLSX, xlsx",
      "presentation.PPTX, pptx",
      "notes.DOC, doc",
      "data.XLS, xls",
      "image.PNG, png"
    })
    @DisplayName("Should extract lowercase extension correctly")
    void shouldExtractExtension(String filename, String expected) {
      assertThat(FileUtils.getExtension(filename)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"noextension", "file.", ""})
    @DisplayName("Should return null when filename has no valid extension")
    void shouldReturnNullWhenNoExtension(String filename) {
      assertThat(FileUtils.getExtension(filename)).isNull();
    }

    @Test
    @DisplayName("Should return null for null filename")
    void shouldReturnNullForNullFilename() {
      assertThat(FileUtils.getExtension((String) null)).isNull();
    }

    @Test
    @DisplayName("Should extract extension from MultipartFile")
    void shouldExtractExtensionFromMultipartFile() {
      MockMultipartFile file =
          new MockMultipartFile("file", "sample.PDF", "application/pdf", new byte[] {1});
      assertThat(FileUtils.getExtension(file)).isEqualTo("pdf");
    }

    @Test
    @DisplayName("Should return null for null MultipartFile or null originalFilename")
    void shouldReturnNullForNullMultipartFile() {
      assertThat(FileUtils.getExtension((MockMultipartFile) null)).isNull();

      MockMultipartFile fileWithNullName =
          new MockMultipartFile("file", null, "application/pdf", new byte[] {1});
      assertThat(FileUtils.getExtension(fileWithNullName)).isNull();
    }
  }

  @Nested
  @DisplayName("resolveDocumentFileType Tests")
  class ResolveDocumentFileTypeTests {

    @Test
    @DisplayName("Should return explicitType when explicitType is provided")
    void shouldReturnExplicitTypeWhenProvided() {
      MockMultipartFile file =
          new MockMultipartFile("file", "presentation.pptx", "application/pdf", new byte[] {1});

      DocumentFileType result =
          FileUtils.resolveDocumentFileType(file, DocumentFileType.PDF);
      assertThat(result).isEqualTo(DocumentFileType.PDF);

      DocumentFileType strResult =
          FileUtils.resolveDocumentFileType("presentation.pptx", DocumentFileType.PDF);
      assertThat(strResult).isEqualTo(DocumentFileType.PDF);
    }

    @ParameterizedTest
    @CsvSource({
      "file.docx, DOCX",
      "FILE.DOCX, DOCX",
      "document.doc, DOC",
      "data.xlsx, XLSX",
      "table.xls, XLS",
      "slides.pptx, PPTX",
      "manual.pdf, PDF",
      "archive.zip, PDF",
      "no_ext, PDF"
    })
    @DisplayName("Should resolve correct DocumentFileType from filename")
    void shouldResolveDocumentFileTypeFromFilename(String filename, DocumentFileType expected) {
      assertThat(FileUtils.resolveDocumentFileType(filename)).isEqualTo(expected);
      assertThat(FileUtils.resolveDocumentFileType(filename, null)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should resolve correct DocumentFileType from MultipartFile")
    void shouldResolveFromMultipartFile() {
      MockMultipartFile docxFile =
          new MockMultipartFile("file", "assignment.docx", "application/msword", new byte[] {1});
      assertThat(FileUtils.resolveDocumentFileType(docxFile)).isEqualTo(DocumentFileType.DOCX);

      MockMultipartFile xlsxFile =
          new MockMultipartFile("file", "data.XLSX", "application/vnd.ms-excel", new byte[] {1});
      assertThat(FileUtils.resolveDocumentFileType(xlsxFile)).isEqualTo(DocumentFileType.XLSX);
    }

    @Test
    @DisplayName("Should default to PDF for null MultipartFile or null filename")
    void shouldDefaultToPdfForNulls() {
      assertThat(FileUtils.resolveDocumentFileType((MockMultipartFile) null))
          .isEqualTo(DocumentFileType.PDF);
      assertThat(FileUtils.resolveDocumentFileType((String) null))
          .isEqualTo(DocumentFileType.PDF);

      MockMultipartFile nullNameFile =
          new MockMultipartFile("file", null, "application/octet-stream", new byte[] {1});
      assertThat(FileUtils.resolveDocumentFileType(nullNameFile))
          .isEqualTo(DocumentFileType.PDF);
    }
  }
}
