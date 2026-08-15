package nicolas.shopee_label_calculator.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalHandler {

    @ExceptionHandler (IllegalArgumentException.class)
    public ResponseEntity<ExceptionResponse> handleIllegalArgumentException (IllegalArgumentException ex) {
        return new ResponseEntity<>( new ExceptionResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<ExceptionResponse> handleException (Exception ex) {
        ex.printStackTrace();
        return new ResponseEntity<>( new ExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
