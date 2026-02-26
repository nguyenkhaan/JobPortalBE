package Cloudian.JobPortal.exceptions.custom;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseException extends RuntimeException {
    private String message;
    private String code;
    public BaseException(String message) {
        this.message = message;
    }
}
