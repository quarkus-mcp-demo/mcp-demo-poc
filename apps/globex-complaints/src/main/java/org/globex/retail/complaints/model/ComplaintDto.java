package org.globex.retail.complaints.model;

import org.globex.retail.complaints.persistence.Complaint;

import java.time.OffsetDateTime;

public class ComplaintDto {

    public Long id;
    public String userId;
    public Long orderId;
    public String productCode;
    public String issueType;
    public String severity;
    public String complaint;
    public String status;
    public String resolution;
    public OffsetDateTime createdAt;
    public OffsetDateTime updatedAt;

    public static ComplaintDto from(Complaint complaint) {
        if (complaint == null) {
            return null;
        }
        ComplaintDto dto = new ComplaintDto();
        dto.id = complaint.id;
        dto.userId = complaint.userId;
        dto.orderId = complaint.orderId;
        dto.productCode = complaint.productCode;
        dto.issueType = complaint.issueType;
        dto.severity = complaint.severity;
        dto.complaint = complaint.complaint;
        dto.status = complaint.status;
        dto.resolution = complaint.resolution;
        dto.createdAt = complaint.createdAt;
        dto.updatedAt = complaint.updatedAt;
        return dto;
    }
}
