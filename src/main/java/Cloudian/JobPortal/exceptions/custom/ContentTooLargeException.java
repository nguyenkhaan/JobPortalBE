package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class ContentTooLargeException extends BaseException {
    public ContentTooLargeException(String message) {

        super(message);
        this.setCode(HttpStatus.CONTENT_TOO_LARGE);
    }
}
