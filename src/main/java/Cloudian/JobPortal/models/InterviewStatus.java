package Cloudian.JobPortal.models;

public enum InterviewStatus {
    PENDING("pending"),
    SCHEDULED("scheduled"),
    COMPLETED("completed"),
    REJECTED("rejected");

    public final String label;

    InterviewStatus(String label) {
        this.label = label;
    }
}