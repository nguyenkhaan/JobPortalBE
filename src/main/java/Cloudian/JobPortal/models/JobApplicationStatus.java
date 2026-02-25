package Cloudian.JobPortal.models;

public enum JobApplicationStatus {
    PENDING("pending"), REVIEWING("reviewing"), INTERVIEW("interview"), OFFER("offer"), REJECTED("rejected"), ACCEPTED("accepted");
   public final String label;
   JobApplicationStatus(String label) {
       this.label = label;
   }  //constructor thi khong can them public hay private, them vao la thanh sai
}
