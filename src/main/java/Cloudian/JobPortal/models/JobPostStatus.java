package Cloudian.JobPortal.models;

public enum JobPostStatus {
    OPEN("Open"),
    DRAFT("Draft"), 
    ACTIVE("Active"), 
    EXPIRED("Expired"), 
    CLOSED("Closed");

    public final String label;
    JobPostStatus(String label) {
        this.label = label;
    }
}
