package com.example.slur.censor.services;

public interface IRefactorEpub<S, T> {

    public String refactorEpubWithProfanityAndSubstitutionForTxt(S bookSection, T epubFile);

    public String refactorEpubWithProfanityAndSubstitutionForSSML(S bookSection, T epubFile);

}
