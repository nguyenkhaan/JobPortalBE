package Cloudian.JobPortal.exceptions.custom;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message);
        this.setCode("403");
    }
}
