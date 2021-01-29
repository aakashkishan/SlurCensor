package com.example.slur.censor.services;

import java.util.List;

public interface IGenerateTxt<S, T> {

    public List<S> readEpubIntoBookSections(T epubFile);

    public boolean writeRefactoredTextToTxtFile(List<String> refactoredTexts, T epubFile);

}
