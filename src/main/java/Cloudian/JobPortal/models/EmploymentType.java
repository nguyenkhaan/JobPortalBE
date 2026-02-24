package Cloudian.JobPortal.models;

public enum EmploymentType {
    FULL_TIME("fulltime"),
    PART_TIME("parttime");
    public final String label;
    EmploymentType(String label) {
        this.label = label;
    }
}
