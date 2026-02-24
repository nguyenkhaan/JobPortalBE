package Cloudian.JobPortal.models;

public enum JobPostStatus {
    OPEN("open"),  CLOSE("close"), DFRAFT("draft");
    public final String label;
    JobPostStatus(String label) {
        this.label = label;
    }
}
