package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class ConflictException extends BaseException {
    public ConflictException(String message) {
        super(message);
        this.setCode(HttpStatus.CONFLICT);
    }
}
