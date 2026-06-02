package Cloudian.JobPortal.events.notification;

public enum NotificationType {
    //1. Employer nhan thong bao co ung vien Apply Job Post
    CANDIDATE_APPLY("Candidate applied job!!!"),
    //2. Employer nhan thong bao tai khaon da duoc cap quyen sau khi mua goi
    EMPLOYER_ACCOUNT_APPROVE("Employer account approved"),
    //3. Admin nhan thong bao co employer mua goi va da thuc hien viec thanh toan
    ADMIN_RECEIVE_PAYMENT_PLAN("Admin receive payment plan");
    public final String label;
    NotificationType(String label) {
        this.label = label;
    }
}
