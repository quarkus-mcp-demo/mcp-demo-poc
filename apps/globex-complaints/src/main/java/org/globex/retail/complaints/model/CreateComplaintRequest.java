package org.globex.retail.complaints.model;

public class CreateComplaintRequest {

    public String userId;
    public Long orderId;
    public String productCode;
    public String issueType;
    public String severity;
    public String complaint;
    public String status;
}
