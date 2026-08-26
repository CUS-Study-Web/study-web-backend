package studyweb.cus.entity.document;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studyweb.cus.entity.AbstractAuditEntity;
import studyweb.cus.entity.badge.Badge;

@Entity
@Table(
    name = "document_badges",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uq_document_badge",
          columnNames = {"document_id", "badge_id"})
    },
    indexes = {
      @Index(name = "idx_document_badges_document", columnList = "document_id"),
      @Index(name = "idx_document_badges_badge", columnList = "badge_id")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentBadge extends AbstractAuditEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "badge_id", nullable = false)
  private Badge badge;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_id", nullable = false)
  private Document document;
}
