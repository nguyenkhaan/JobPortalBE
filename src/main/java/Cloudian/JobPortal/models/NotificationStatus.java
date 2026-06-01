package Cloudian.JobPortal.models;

public enum NotificationStatus {
    SENT("Sent"),
    PENDING("Pending"),
    FAILED("Failed");
    public final String label;
    NotificationStatus(String label) {
        this.label = label;
    }
}
