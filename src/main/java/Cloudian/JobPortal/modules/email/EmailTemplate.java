package Cloudian.JobPortal.modules.email;

public enum EmailTemplate {
    RESET_PASSWORD("/email/reset-password"), 
    VERIFY_REGISTER("email/verify_register"); 
    private final String path;
    EmailTemplate(String path) {
        this.path = path;
    }
    
    public String getPath()
    {
        return path;
    }
}
