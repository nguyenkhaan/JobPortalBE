package Cloudian.JobPortal.modules.jobpost;

import lombok.Getter;

@Getter
public enum JobPostSortBy {
    LATEST("Latest"),
    OLDEST("Oldest"),
    HIGHEST_SALARY("Highest Salary");

    private final String label;

    JobPostSortBy(String label) {
        this.label = label;
    }

    public static JobPostSortBy fromString(String value) {
        if (value == null || value.isBlank()) {
            return LATEST; // default
        }
        try {
            return valueOf(value.trim().toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return LATEST; // default if invalid value
        }
    }
}