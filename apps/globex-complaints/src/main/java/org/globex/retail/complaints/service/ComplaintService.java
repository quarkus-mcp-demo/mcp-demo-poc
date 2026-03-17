package org.globex.retail.complaints.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.globex.retail.complaints.model.ComplaintDto;
import org.globex.retail.complaints.model.CreateComplaintRequest;
import org.globex.retail.complaints.model.UpdateComplaintRequest;
import org.globex.retail.complaints.persistence.Complaint;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class ComplaintService {

    @Transactional
    public ComplaintDto createComplaint(CreateComplaintRequest request) {
        Complaint complaint = new Complaint();
        complaint.userId = request.userId;
        complaint.orderId = request.orderId;
        complaint.productCode = request.productCode;
        complaint.issueType = request.issueType;
        complaint.severity = request.severity;
        complaint.complaint = request.complaint;
        complaint.status = request.status;

        complaint.persist();
        return ComplaintDto.from(complaint);
    }

    @Transactional
    public Optional<ComplaintDto> updateComplaint(Long id, UpdateComplaintRequest request) {
        Complaint complaint = Complaint.findById(id);
        if (complaint == null) {
            return Optional.empty();
        }

        if (request.issueType != null) {
            complaint.issueType = request.issueType;
        }
        if (request.severity != null) {
            complaint.severity = request.severity;
        }
        if (request.complaint != null) {
            complaint.complaint = request.complaint;
        }
        if (request.status != null) {
            complaint.status = request.status;
        }
        if (request.resolution != null) {
            complaint.resolution = request.resolution;
        }

        complaint.persist();
        return Optional.of(ComplaintDto.from(complaint));
    }

    @Transactional
    public List<ComplaintDto> findByUserId(String userId) {
        return Complaint.list("userId", userId).stream().map(complaint -> ComplaintDto.from((Complaint) complaint)).collect(Collectors.toList());
    }

    @Transactional
    public List<ComplaintDto> findByProductCode(String productCode) {
        return Complaint.list("productCode", productCode).stream().map(complaint -> ComplaintDto.from((Complaint) complaint)).collect(Collectors.toList());
    }

    @Transactional
    public Optional<ComplaintDto> findById(Long id) {
        return Optional.ofNullable(ComplaintDto.from(Complaint.findById(id)));
    }

    @Transactional
    public List<ComplaintDto> findAll() {
        return Complaint.listAll().stream().map(complaint -> ComplaintDto.from((Complaint) complaint)).collect(Collectors.toList());
    }
}
