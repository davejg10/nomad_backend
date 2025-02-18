package com.nomad.backend.exceptions;

public class NotFoundRequestException extends RuntimeException {

    NotFoundRequestException() { super("Not found, please try again."); }
    public NotFoundRequestException(String details) { super(details);}


}
