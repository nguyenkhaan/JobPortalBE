package Cloudian.JobPortal.models;

// enum cho trạng thái duyệt của employer do admin quyết định
public enum ApprovalStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    public final String label;
    ApprovalStatus(String label) {
        this.label = label;
    }
}
