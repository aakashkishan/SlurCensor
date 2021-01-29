package com.example.slur.censor.exceptions;

public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException(String s) {
        super(s);
    }

    public FileNotFoundException(String s, Throwable t) {
        super(s, t);
    }

}
