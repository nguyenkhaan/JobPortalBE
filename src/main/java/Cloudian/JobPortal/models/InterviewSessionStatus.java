package Cloudian.JobPortal.models;

public enum InterviewSessionStatus {
    PENDING_SELECTION("pending selection"),
    CONFIRMED("confirmed"),
    EXPIRED("expired"),
    CANCELLED("cancelled"),
    COMPLETED("completed");

    public final String label;

    InterviewSessionStatus(String label) {
        this.label = label;
    }
}
