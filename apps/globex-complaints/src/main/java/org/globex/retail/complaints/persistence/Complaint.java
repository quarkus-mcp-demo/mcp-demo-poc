package org.globex.retail.complaints.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.List;

@Entity(name = "Complaint")
@Table(name = "complaints")
public class Complaint extends PanacheEntityBase {

    @Id
    @SequenceGenerator(
        name = "complaintsSequence",
        sequenceName = "complaints_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "complaintsSequence")
    @Column(name = "id")
    public Long id;

    @Column(name = "user_id")
    public String userId;

    @Column(name = "order_id")
    public Long orderId;

    @Column(name = "product_code")
    public String productCode;

    @Column(name = "issue_type")
    public String issueType;

    @Column(name = "severity")
    public String severity;

    @Column(name = "complaint", columnDefinition = "TEXT")
    public String complaint;

    @Column(name = "status")
    public String status;

    @Column(name = "resolution", columnDefinition = "TEXT")
    public String resolution;

    @Column(name = "created_at", nullable = false, updatable = false)
    public OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    public OffsetDateTime updatedAt;

    @Version
    @Column(name = "version")
    public int version;

    /**
     * Retrieves complaints by product code and time range.
     * Results are sorted by severity (critical > high > medium > low) and then by created time.
     *
     * @param productCode the product code to filter by
     * @param startTime the start of the time range (inclusive)
     * @param endTime the end of the time range (inclusive)
     * @return list of complaints matching the criteria
     */
    public static List<Complaint> findByProductCodeAndTimeRange(
            String productCode,
            OffsetDateTime startTime,
            OffsetDateTime endTime) {
        return find(
            "SELECT c FROM Complaint c " +
            "WHERE c.productCode = ?1 " +
            "AND c.createdAt >= ?2 " +
            "AND c.createdAt <= ?3 " +
            "ORDER BY " +
            "CASE c.severity " +
            "  WHEN 'critical' THEN 1 " +
            "  WHEN 'high' THEN 2 " +
            "  WHEN 'medium' THEN 3 " +
            "  WHEN 'low' THEN 4 " +
            "  ELSE 5 " +
            "END, " +
            "c.createdAt DESC",
            productCode, startTime, endTime
        ).list();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
