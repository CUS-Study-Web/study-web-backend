package studyweb.cus.repository.user;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studyweb.cus.entity.user.User;
import studyweb.cus.entity.user.VipRequest;
import studyweb.cus.enums.UserRole;
import studyweb.cus.enums.VipRequestStatus;

public interface VipRequestRepository extends JpaRepository<VipRequest, UUID> {

  boolean existsByUserAndStatus(User user, VipRequestStatus status);

  @Query(
      value =
          """
          SELECT vr FROM VipRequest vr
          JOIN FETCH vr.user u
          WHERE (:role IS NULL OR u.role = :role)
            AND (:status IS NULL OR vr.status = :status)
            AND (CAST(:search AS string) IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(vr.note) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """,
      countQuery =
          """
          SELECT COUNT(vr) FROM VipRequest vr
          JOIN vr.user u
          WHERE (:role IS NULL OR u.role = :role)
            AND (:status IS NULL OR vr.status = :status)
            AND (CAST(:search AS string) IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))
                 OR LOWER(vr.note) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')))
          """)
  Page<VipRequest> searchVipRequests(
      @Param("search") String search,
      @Param("status") VipRequestStatus status,
      @Param("role") UserRole role,
      Pageable pageable);

  @Query(
      """
      SELECT COUNT(vr) FROM VipRequest vr
      JOIN vr.user u
      WHERE u.role = 'LEARNER'
      """)
  int countTotal();

  @Query(
      """
      SELECT COUNT(vr) FROM VipRequest vr
      JOIN vr.user u
      WHERE u.role = 'LEARNER'
        AND vr.status = :status
      """)
  int countByStatus(@Param("status") VipRequestStatus status);

  @Modifying
  @Query(
      "UPDATE VipRequest vr SET vr.status = studyweb.cus.enums.VipRequestStatus.APPROVED "
          + "WHERE vr.id = :id AND vr.status = studyweb.cus.enums.VipRequestStatus.WAITING")
  int approveVip(@Param("id") UUID id);

  @Modifying
  @Query(
      "UPDATE VipRequest vr SET vr.status = studyweb.cus.enums.VipRequestStatus.DECLINED "
          + "WHERE vr.id = :id AND vr.status = studyweb.cus.enums.VipRequestStatus.WAITING")
  int disapproveVip(@Param("id") UUID id);
}
