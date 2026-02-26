package Cloudian.JobPortal.exceptions.custom;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

@Getter
@Setter
public class BaseException extends RuntimeException {
    private String message;
    private HttpStatusCode code;
    public BaseException(String message) {
        this.message = message;
    }
}
