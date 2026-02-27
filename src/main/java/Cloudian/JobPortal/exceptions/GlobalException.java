package Cloudian.JobPortal.exceptions;

import Cloudian.JobPortal.exceptions.custom.*;
import Cloudian.JobPortal.exceptions.dto.ExceptionDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

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

    //Validator exceptions
    //Document:
    @ExceptionHandler(MethodArgumentNotValidException.class)
    //https://www.baeldung.com/spring-boot-bean-validation
    public ResponseEntity<?>  handleValidationExceptions(
            MethodArgumentNotValidException ex)
    {
        HashMap<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {

            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

}
