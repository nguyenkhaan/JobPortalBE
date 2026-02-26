package Cloudian.JobPortal.exceptions.custom;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message);
        this.setCode("409");
    }
}
