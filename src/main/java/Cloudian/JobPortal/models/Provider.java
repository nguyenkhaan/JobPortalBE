package Cloudian.JobPortal.models;

public enum Provider {
    LOCAL("local"),
    GOOGLE("google"); 
    private final String value;

    Provider(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
