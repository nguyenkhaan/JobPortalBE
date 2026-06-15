package Cloudian.JobPortal.models;

public enum ApprovalStatus {
    PENDING("pending"),
    APPROVED("approved"),
    REJECTED("rejected");

    public final String label;
    ApprovalStatus(String label) {
        this.label = label;
    }
}
