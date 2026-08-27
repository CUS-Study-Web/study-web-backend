package studyweb.cus.controller.document;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.document.CreateDocumentRequest;
import studyweb.cus.dto.request.document.UpdateDocumentRequest;
import studyweb.cus.dto.response.badge.BadgeResponse;
import studyweb.cus.dto.response.document.DocumentDownloadResponse;
import studyweb.cus.dto.response.document.DocumentResponse;
import studyweb.cus.enums.AccessTier;
import studyweb.cus.enums.DocType;
import studyweb.cus.enums.DocumentFileType;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.document.DocumentService;

@WebMvcTest(
    controllers = DocumentController.class,
    excludeFilters =
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = JwtAuthenticationFilter.class))
@Import(ResponseFactory.class)
class DocumentControllerTest {

  private static final UUID DOCUMENT_ID = UUID.randomUUID();
  private static final UUID BADGE_ID = UUID.randomUUID();

  @Autowired private MockMvc mockMvc;

  @MockitoBean private DocumentService documentService;

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  private static RequestPostProcessor authenticated() {
    return SecurityMockMvcRequestPostProcessors.authentication(
        new UsernamePasswordAuthenticationToken(
            "learner@test.com", null, List.of(new SimpleGrantedAuthority("ROLE_LEARNER"))));
  }

  private MockMultipartFile mockFile() {
    return new MockMultipartFile(
        "file", "guide.pdf", "application/pdf", "dummy-content".getBytes());
  }

  private DocumentResponse sampleDocumentResponse() {
    return new DocumentResponse(
        DOCUMENT_ID,
        "Grammar Guide",
        DocType.THEORY,
        DocumentFileType.PDF,
        "https://s3.example.com/guide.pdf",
        12,
        "A comprehensive guide",
        0,
        "https://youtube.com/watch?v=123",
        AccessTier.PUBLIC,
        List.of(
            new BadgeResponse(
                BADGE_ID, "Toán", null, LocalDateTime.now(), LocalDateTime.now())),
        LocalDateTime.now(),
        LocalDateTime.now());
  }

  // --- Upload Tests ---

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("POST /api/documents - Assistant role allowed to upload")
  void uploadDocument_assistantAllowed() throws Exception {
    when(documentService.uploadDocument(any(CreateDocumentRequest.class)))
        .thenReturn(sampleDocumentResponse());

    mockMvc
        .perform(
            multipart("/api/documents")
                .file(mockFile())
                .param("title", "Grammar Guide")
                .param("docType", "THEORY")
                .param("accessTier", "PUBLIC")
                .param("youtubeUrl", "https://youtube.com/watch?v=123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.title").value("Grammar Guide"))
        .andExpect(jsonPath("$.data.badges[0].name").value("Toán"));
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  @DisplayName("POST /api/documents - Learner role forbidden from uploading")
  void uploadDocument_learnerForbidden() throws Exception {
    mockMvc
        .perform(
            multipart("/api/documents")
                .file(mockFile())
                .param("title", "Grammar Guide")
                .param("youtubeUrl", "https://youtube.com/watch?v=123"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("POST /api/documents - Admin role forbidden from uploading (assistant only)")
  void uploadDocument_adminForbidden() throws Exception {
    mockMvc
        .perform(
            multipart("/api/documents")
                .file(mockFile())
                .param("title", "Grammar Guide")
                .param("youtubeUrl", "https://youtube.com/watch?v=123"))
        .andExpect(status().isForbidden());
  }

  // --- Watch/View Detail Tests ---

  @Test
  @DisplayName("GET /api/documents/{id} - Learner can view/watch document details")
  void getDocumentDetail_authenticatedAllowed() throws Exception {
    when(documentService.getDocumentById(eq(DOCUMENT_ID), eq("learner@test.com")))
        .thenReturn(sampleDocumentResponse());

    mockMvc
        .perform(get("/api/documents/{id}", DOCUMENT_ID).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(DOCUMENT_ID.toString()))
        .andExpect(jsonPath("$.data.title").value("Grammar Guide"))
        .andExpect(jsonPath("$.data.fileUrl").value("https://s3.example.com/guide.pdf"))
        .andExpect(jsonPath("$.data.badges[0].name").value("Toán"));
  }

  // --- List Documents Tests ---

  @Test
  @DisplayName("GET /api/documents - List documents with pagination")
  void listDocuments_authenticatedAllowed() throws Exception {
    Pageable pageable = PageRequest.of(0, 10);
    when(documentService.listDocuments(any(), any(), any(), any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(sampleDocumentResponse()), pageable, 1));

    mockMvc
        .perform(get("/api/documents").with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data[0].title").value("Grammar Guide"))
        .andExpect(jsonPath("$.data[0].badges[0].name").value("Toán"));
  }

  // --- Download Tests ---

  @Test
  @DisplayName("GET /api/documents/{id}/download - Download document endpoint")
  void downloadDocument_authenticatedAllowed() throws Exception {
    DocumentDownloadResponse downloadResponse =
        new DocumentDownloadResponse(
            DOCUMENT_ID,
            "Grammar Guide",
            "https://s3.example.com/guide.pdf",
            DocumentFileType.PDF,
            1);

    when(documentService.downloadDocument(eq(DOCUMENT_ID), eq("learner@test.com")))
        .thenReturn(downloadResponse);

    mockMvc
        .perform(get("/api/documents/{id}/download", DOCUMENT_ID).with(authenticated()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.downloadUrl").value("https://s3.example.com/guide.pdf"))
        .andExpect(jsonPath("$.data.downloadCount").value(1));
  }

  // --- Update & Delete Tests ---

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("PUT /api/documents/{id} - Assistant allowed to update")
  void updateDocument_assistantAllowed() throws Exception {
    when(documentService.updateDocument(eq(DOCUMENT_ID), any(UpdateDocumentRequest.class)))
        .thenReturn(sampleDocumentResponse());

    mockMvc
        .perform(
            MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/api/documents/{id}", DOCUMENT_ID)
                .param("title", "Updated Guide")
                .param("youtubeUrl", "https://youtube.com/watch?v=123"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  @DisplayName("PUT /api/documents/{id} - Admin forbidden from updating")
  void updateDocument_adminForbidden() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/api/documents/{id}", DOCUMENT_ID)
                .param("title", "Updated Guide")
                .param("youtubeUrl", "https://youtube.com/watch?v=123"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ASSISTANT")
  @DisplayName("DELETE /api/documents/{id} - Assistant allowed to delete")
  void deleteDocument_assistantAllowed() throws Exception {
    mockMvc
        .perform(delete("/api/documents/{id}", DOCUMENT_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200));

    verify(documentService).deleteDocument(DOCUMENT_ID);
  }

  @Test
  @WithMockUser(roles = "LEARNER")
  @DisplayName("DELETE /api/documents/{id} - Learner forbidden from deleting")
  void deleteDocument_learnerForbidden() throws Exception {
    mockMvc
        .perform(delete("/api/documents/{id}", DOCUMENT_ID))
        .andExpect(status().isForbidden());
  }
}
