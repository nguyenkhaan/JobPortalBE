package Cloudian.JobPortal.exceptions.custom;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message);
        this.setCode("402");
    }
}
