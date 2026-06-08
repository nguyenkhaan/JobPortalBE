package Cloudian.JobPortal.modules.employer.dto;

import Cloudian.JobPortal.models.ApprovalStatus;
import lombok.Data;

@Data
public class UpdateEmployerApprovalRequest {
    private ApprovalStatus approvalStatus;
    private String rejectionReason;
}
