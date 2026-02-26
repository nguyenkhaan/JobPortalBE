package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BaseException {
    public UnauthorizedException(String message) {
        super(message);
        this.setCode(HttpStatus.UNAUTHORIZED);
    }
}
