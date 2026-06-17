package Cloudian.JobPortal.events.notification;

public enum NotificationType {
    //1. Employer nhan thong bao co ung vien Apply Job Post
    CANDIDATE_APPLY("Candidate applied job!!!"),
    //2. Employer nhan thong bao tai khaon da duoc cap quyen sau khi mua goi
    EMPLOYER_ACCOUNT_APPROVE("Employer account approved"),
    //3. Admin nhan thong bao co employer mua goi va da thuc hien viec thanh toan
    ADMIN_RECEIVE_PAYMENT_PLAN("Admin receive payment plan"),
    EMPLOYER_PROFILE_SUBMITTED("Employer profile submitted"),
    EMPLOYER_PROFILE_APPROVED("Employer profile approved"),
    EMPLOYER_PROFILE_REJECTED("Employer profile rejected"),
    PAYMENT_SUBMITTED("Payment submitted"),
    PAYMENT_APPROVED("Payment approved"),
    APPLICATION_ACCEPTED("Application accepted"),
    APPLICATION_REJECTED("Application rejected"),
    INTERVIEW_PROPOSED("Interview proposed"),
    INTERVIEW_CONFIRMED("Interview confirmed"),
    INTERVIEW_EXPIRED("Interview expired"),
    INTERVIEW_CANCELLED("Interview cancelled"),
    INTERVIEW_COMPLETED("Interview completed");
    public final String label;
    NotificationType(String label) {
        this.label = label;
    }
}
