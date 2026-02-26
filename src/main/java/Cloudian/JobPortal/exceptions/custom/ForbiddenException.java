package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends BaseException {
    public ForbiddenException(String message) {
        super(message);
        this.setCode(HttpStatus.FORBIDDEN);
    }
}
