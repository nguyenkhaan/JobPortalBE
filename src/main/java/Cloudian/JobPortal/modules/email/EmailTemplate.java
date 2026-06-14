package Cloudian.JobPortal.modules.email;

public enum EmailTemplate {
    RESET_PASSWORD("email/reset-password"),     // Bỏ dấu / ở đầu
    VERIFY_REGISTER("email/verify_register"),   // Giữ nguyên (nhớ check lại file thực tế là gạch dưới _ hay gạch nối -)
    CANDIDATE_INVITATION("email/candidate-invitation"); // Giữ nguyên

    private final String path;
    EmailTemplate(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}