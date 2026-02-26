package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseException {
    public InternalServerException(String message) {
        super(message);
        this.setCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
