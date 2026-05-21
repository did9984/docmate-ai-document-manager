package com.park.docmate.exception;

public class DuplicateUsernameException extends RuntimeException{

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
