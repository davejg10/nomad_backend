package com.nomad.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class NotFoundRequestAdvice {

    @ResponseBody
    @ExceptionHandler(NotFoundRequestException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String notFoundRequestHandler(NotFoundRequestException ex) {
        return ex.getMessage();
    }
}
