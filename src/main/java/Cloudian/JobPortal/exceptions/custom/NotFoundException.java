package Cloudian.JobPortal.exceptions.custom;

import org.springframework.http.HttpStatus;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message);
      this.setCode(HttpStatus.NOT_FOUND);
    }
}
