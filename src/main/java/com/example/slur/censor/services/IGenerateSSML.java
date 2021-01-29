package com.example.slur.censor.services;

import java.util.List;

public interface IGenerateSSML<S, T> {

    public List<S> readEpubIntoBookSections(T epubFile);

    public boolean writeRefactoredTextToSSMLFile(List<String> refactoredTexts, T epubFile);

}
