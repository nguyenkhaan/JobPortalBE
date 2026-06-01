package Cloudian.JobPortal.models;

public enum EmploymentType {
    FULL_TIME("Full Time"),
    PART_TIME("Part Time"),
    INTERNSHIP("Internship"),
    CONTRACT("Contract Base"),
    TEMPORARY("Temporary");

    public final String label;
    EmploymentType(String label) {
        this.label = label;
    }
}
