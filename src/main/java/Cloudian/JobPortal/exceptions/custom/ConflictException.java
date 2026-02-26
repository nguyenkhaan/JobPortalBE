package Cloudian.JobPortal.exceptions.custom;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message);
        this.setCode("409");
    }
}
