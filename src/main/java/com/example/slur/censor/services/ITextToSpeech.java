package com.example.slur.censor.services;

public interface ITextToSpeech<T> {

    public boolean synthesizeSSML(T epubFile);

}
