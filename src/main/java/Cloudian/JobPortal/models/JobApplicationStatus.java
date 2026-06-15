package Cloudian.JobPortal.models;

public enum JobApplicationStatus {
    PENDING("pending"), REVIEWING("reviewing"), INTERVIEW("interview"), OFFER("offer"), REJECTED("rejected"), ACCEPTED("accepted");
   public final String label;
   JobApplicationStatus(String label) {
       this.label = label;
   }
}
