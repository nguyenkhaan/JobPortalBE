package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) {
        super(message);
        this.setCode(HttpStatus.CONFLICT);
    }
}
