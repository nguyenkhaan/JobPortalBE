package Cloudian.JobPortal.exceptions.custom;

public class InternalServerException extends BaseException {
    public InternalServerException(String message) {
        super(message);
        this.setCode("500");
    }
}
