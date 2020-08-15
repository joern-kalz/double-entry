package com.github.joern.kalz.doubleentry.controllers.exceptions;

import com.github.joern.kalz.doubleentry.generated.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandlerAdvice.class);

    @ExceptionHandler(ParameterException.class)
    public ResponseEntity<Object> handleParameterException(ParameterException exception) {
        ErrorResponse errorResponse = new ErrorResponse().message(exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public ResponseEntity<Object> handleAlreadyExistsException(AlreadyExistsException exception) {
        ErrorResponse errorResponse = new ErrorResponse().message(exception.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception exception) {
        LOG.error("unexpected exception: {}", exception.toString());
        ErrorResponse errorResponse = new ErrorResponse().message("unexpected exception");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
