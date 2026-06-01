package Cloudian.JobPortal.models;

public enum SalaryType {
    MONTHLY("monthly"),
    YEARLY("yearly"),
    HOURLY("hourly");

    public final String label;
    SalaryType(String label) {
        this.label = label;
    }
}