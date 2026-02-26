package Cloudian.JobPortal.exceptions.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ExceptionDto {
    String message;
    LocalDateTime timestamp;
    String code;
    public ExceptionDto(String message , String code)
    {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.code = code;
    }
}
