package Cloudian.JobPortal.models;

public enum OrganizationType {
    PRIVATE_COMPANY("Private Company"),
    PUBLIC_COMPANY("Public_Company"),
    NON_PROFIT("Non-profit");
    public final String label;
    OrganizationType(String label) {
        this.label = label;
    }
}
