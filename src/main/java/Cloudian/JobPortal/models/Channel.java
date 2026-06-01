package Cloudian.JobPortal.models;

public enum Channel {
    IN_APP("in_app"), DEVICE("device");
    public final String label;
    Channel(String label) {
        this.label = label;
    }
}
