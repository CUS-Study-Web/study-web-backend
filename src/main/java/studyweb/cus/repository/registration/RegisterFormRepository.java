package studyweb.cus.repository.registration;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import studyweb.cus.entity.registration.RegisterForm;

@Repository
public interface RegisterFormRepository extends JpaRepository<RegisterForm, UUID> {

  @Query(
      value =
          """
          SELECT rf FROM RegisterForm rf
          WHERE (CAST(:date AS date) IS NULL OR rf.registeredDate = :date)
            AND (CAST(:search AS string) IS NULL OR LOWER(rf.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(rf.phoneNumer) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(rf.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(rf.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """,
      countQuery =
          """
          SELECT COUNT(rf) FROM RegisterForm rf
          WHERE (CAST(:date AS date) IS NULL OR rf.registeredDate = :date)
            AND (CAST(:search AS string) IS NULL OR LOWER(rf.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(rf.phoneNumer) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(rf.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
             OR LOWER(rf.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """)
  Page<RegisterForm> searchRegisterForms(
      @Param("date") LocalDate date, @Param("search") String search, Pageable pageable);

  @Query(
      """
      SELECT COUNT(rf) FROM RegisterForm rf
      WHERE (CAST(:date AS date) IS NULL OR rf.registeredDate = :date)
        AND (CAST(:search AS string) IS NULL OR LOWER(rf.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
         OR LOWER(rf.phoneNumer) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
         OR LOWER(rf.email) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
         OR LOWER(rf.subject) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
      """)
  long countRegisterForms(@Param("date") LocalDate date, @Param("search") String search);
}
