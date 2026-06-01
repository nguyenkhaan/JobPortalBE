package Cloudian.JobPortal.models;

public enum JobPostStatus {
    DRAFT("Draft"),
    ACTIVE("Active"),
    EXPIRED("Expire");

    public final String label;
    JobPostStatus(String label) {
        this.label = label;
    }
}
