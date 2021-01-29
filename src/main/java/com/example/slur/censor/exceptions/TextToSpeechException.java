package com.example.slur.censor.exceptions;

public class TextToSpeechException extends RuntimeException {

    public TextToSpeechException(String s) {
        super(s);
    }

    public TextToSpeechException(String s, Throwable t) {
        super(s, t);
    }

}
