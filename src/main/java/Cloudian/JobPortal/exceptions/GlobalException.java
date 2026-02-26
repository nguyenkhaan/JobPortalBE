package Cloudian.JobPortal.exceptions;

import Cloudian.JobPortal.exceptions.custom.*;
import Cloudian.JobPortal.exceptions.dto.ExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<?> handleBadRequestException(BadRequestException e)
    {
        return ResponseEntity.status(e.getCode()).body(
                new ExceptionDto(e.getMessage() , e.getCode())
        );
    }
    @ExceptionHandler(value = InternalServerException.class)
    public ResponseEntity<?> handleInternalServerException(InternalServerException e)
    {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ExceptionDto(e.getMessage() , e.getCode())
        );
    }
    @ExceptionHandler(value = ConflictException.class)
    public ResponseEntity<?> handleConflictException(ConflictException e)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                new ExceptionDto(e.getMessage() , e.getCode())
        );
    }
    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceAccessException(ResourceNotFoundException e)
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ExceptionDto(e.getMessage() , e.getCode())
        );
    }
    @ExceptionHandler(value = UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException e)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                new ExceptionDto(e.getMessage() , e.getCode())
        );
    }

}
