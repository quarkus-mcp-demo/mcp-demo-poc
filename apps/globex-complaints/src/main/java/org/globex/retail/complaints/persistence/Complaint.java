package org.globex.retail.complaints.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

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
