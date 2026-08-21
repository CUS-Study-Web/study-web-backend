package studyweb.cus.repository.user;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import studyweb.cus.entity.user.VipRequest;
import studyweb.cus.enums.VipRequestStatus;

public interface VipRequestRepository extends JpaRepository<VipRequest, UUID> {

  @Query(
      value =
          """
          SELECT vr FROM VipRequest vr
          JOIN FETCH vr.user u
          WHERE (:status IS NULL OR vr.status = :status)
            AND (:search IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(vr.note) LIKE LOWER(CONCAT('%', :search, '%')))
          """,
      countQuery =
          """
          SELECT COUNT(vr) FROM VipRequest vr
          JOIN vr.user u
          WHERE (:status IS NULL OR vr.status = :status)
            AND (:search IS NULL OR LOWER(u.gmail) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(u.name) LIKE LOWER(CONCAT('%', :search, '%'))
                 OR LOWER(vr.note) LIKE LOWER(CONCAT('%', :search, '%')))
          """)
  Page<VipRequest> searchVipRequests(
      @Param("search") String search, @Param("status") VipRequestStatus status, Pageable pageable);

  @Query(
      """
      SELECT COUNT(vr) FROM VipRequest vr
      JOIN vr.user u
      """)
  int countTotal();

  @Query(
      """
      SELECT COUNT(vr) FROM VipRequest vr
      JOIN vr.user u
      WHERE vr.status = :status
      """)
  int countByStatus(@Param("status") VipRequestStatus status);
}
