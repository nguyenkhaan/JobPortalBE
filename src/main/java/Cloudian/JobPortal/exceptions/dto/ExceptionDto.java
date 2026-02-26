package Cloudian.JobPortal.exceptions.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExceptionDto {
    String message;
    LocalDateTime timestamp;
    private int code;
    public ExceptionDto(String message , HttpStatusCode code)
    {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.code = code.value();
    }
}
