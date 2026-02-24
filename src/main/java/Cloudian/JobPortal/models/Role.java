package Cloudian.JobPortal.models;

public enum Role {
    SEEKER("seeker"),
    EMPLOYER("employer"),
    ADMIN("admin");

    public final String label;
    Role(String label) {
        this.label = label;
    }
}
