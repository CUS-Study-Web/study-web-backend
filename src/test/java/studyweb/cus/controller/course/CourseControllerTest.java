package studyweb.cus.controller.course;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import studyweb.cus.controller.ResponseFactory;
import studyweb.cus.dto.request.course.CourseRequest;
import studyweb.cus.dto.response.course.CourseDetailResponse;
import studyweb.cus.dto.response.course.CourseSummaryResponse;
import studyweb.cus.dto.response.course.SubjectSummaryResponse;
import studyweb.cus.security.JwtAuthenticationFilter;
import studyweb.cus.service.course.CourseService;

@WebMvcTest(CourseController.class)
@Import(ResponseFactory.class)
class CourseControllerTest {

  private static final UUID COURSE_ID = UUID.randomUUID();

  @Autowired private WebApplicationContext wac;

  private MockMvc mockMvc;

  @MockitoBean private CourseService courseService;

  @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

  @TestConfiguration
  @EnableMethodSecurity
  static class SliceSecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(
              auth ->
                  auth.requestMatchers("/api/auth/**")
                      .permitAll()
                      .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/*")
                      .permitAll()
                      .anyRequest()
                      .authenticated())
          .httpBasic(Customizer.withDefaults());
      return http.build();
    }
  }

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(wac).apply(springSecurity()).build();
  }

  private CourseSummaryResponse summary() {
    return new CourseSummaryResponse(COURSE_ID, "Java", "sub", "badge", "desc", "url");
  }

  private MockMultipartFile thumbnail() {
    return new MockMultipartFile("thumbnailImage", "thumb.png", "image/png", new byte[] {1});
  }

  @Test
  void listCourses_isPublicAndReturnsData() throws Exception {
    when(courseService.listCourses(any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(summary()), PageRequest.of(0, 10), 1));

    mockMvc
        .perform(get("/api/courses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.message").value("Courses fetched successfully!"))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data[0].id").value(COURSE_ID.toString()))
        .andExpect(jsonPath("$.paging.page").value(0))
        .andExpect(jsonPath("$.paging.limit").value(10))
        .andExpect(jsonPath("$.paging.total").value(1))
        .andExpect(jsonPath("$.paging.totalPages").value(1));

    verify(courseService).listCourses(any(Pageable.class));
  }

  @Test
  void courseDetail_guestIsAllowed() throws Exception {
    CourseDetailResponse detail =
        CourseDetailResponse.of(
            2, null, List.of(new SubjectSummaryResponse(UUID.randomUUID(), "Basics", null, 3)));
    when(courseService.getCourseDetail(eq(COURSE_ID), any())).thenReturn(detail);

    mockMvc
        .perform(get("/api/courses/{id}", COURSE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.totalSubjects").value(2));

    verify(courseService).getCourseDetail(eq(COURSE_ID), any());
  }

  @Test
  void createCourse_unauthenticatedIsRejected() throws Exception {
    mockMvc
        .perform(multipart("/api/courses").file(thumbnail()).param("title", "Java"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "USER")
  void createCourse_nonAdminForbidden() throws Exception {
    mockMvc
        .perform(multipart("/api/courses").file(thumbnail()).param("title", "Java"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createCourse_adminAllowed() throws Exception {
    when(courseService.createCourse(any(CourseRequest.class))).thenReturn(summary());

    mockMvc
        .perform(multipart("/api/courses").file(thumbnail()).param("title", "Java"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statusCode").value(200))
        .andExpect(jsonPath("$.data.id").value(COURSE_ID.toString()));

    verify(courseService).createCourse(any(CourseRequest.class));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createCourse_missingThumbnailBadRequest() throws Exception {
    mockMvc
        .perform(multipart("/api/courses").param("title", "Java"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("COURSE_006"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void createCourse_missingTitleBadRequest() throws Exception {
    mockMvc
        .perform(multipart("/api/courses").file(thumbnail()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errorCode").value("SYS_002"));
  }

  @Test
  @WithMockUser
  void createCourse_putMethodNotAllowed() throws Exception {
    mockMvc
        .perform(put("/api/courses"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.statusCode").value(405));
  }

  @Test
  @WithMockUser
  void courseDetail_postMethodNotAllowed() throws Exception {
    mockMvc.perform(post("/api/courses/{id}", COURSE_ID)).andExpect(status().isMethodNotAllowed());
  }

  @Test
  @WithMockUser(roles = "USER")
  void deleteCourse_nonAdminForbidden() throws Exception {
    mockMvc.perform(delete("/api/courses/{id}", COURSE_ID)).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void deleteCourse_adminAllowed() throws Exception {
    mockMvc
        .perform(delete("/api/courses/{id}", COURSE_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Course deleted successfully!"));

    verify(courseService).deleteCourse(COURSE_ID);
  }
}