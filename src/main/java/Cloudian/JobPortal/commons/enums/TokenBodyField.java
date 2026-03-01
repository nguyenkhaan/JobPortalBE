package Cloudian.JobPortal.commons.enums;

public enum TokenBodyField {
    EMAIL("email"), ID("id"), PURPOSE("purpose"), EXPIRES("expires"), ROLES("roles");
    private final String label;
    TokenBodyField(String label) {
        this.label = label;
    }
}
//Roles co them s hay khong ????