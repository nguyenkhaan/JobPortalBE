package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BaseException {
    public BadRequestException(String message) {
        super(message);
        this.setCode(HttpStatus.BAD_REQUEST);
    }
}