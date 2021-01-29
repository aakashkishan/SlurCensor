package com.example.slur.censor.services.impl;

import com.example.slur.censor.model.EpubFile;
import com.example.slur.censor.services.IRefactorEpub;
import com.example.slur.censor.utils.SSMLConst;
import com.github.mertakdut.BookSection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RefactorEpub implements IRefactorEpub<BookSection, EpubFile> {

    public String refactorEpubWithProfanityAndSubstitutionForTxt(BookSection bookSection, EpubFile epubFile) {

        String bookSectionText = bookSection.getSectionTextContent();
        String[] bookSectionWords = bookSectionText.split(" ");

        List<String> censoredWords = Arrays.stream(bookSectionWords)
                .map(word -> epubFile.profanityMap.keySet().contains(word) ? epubFile.profanityMap.get(word) : word)
                .map(word -> epubFile.substituteMap.keySet().contains(word) ? epubFile.substituteMap.get(word) : word)
                .collect(Collectors.toList());

        censoredWords.stream().forEach(System.out::println);

        return StringUtils.join(censoredWords, " ");
    }

    public String refactorEpubWithProfanityAndSubstitutionForSSML(BookSection bookSection, EpubFile epubFile) {

        String bookSectionText = bookSection.getSectionTextContent();
        String[] bookSectionWords = bookSectionText.split(" ");

        List<String> censoredWords = Arrays.stream(bookSectionWords)
                .map(word -> epubFile.profanityMap.keySet().contains(word) ? String.format("%s%s%s",
                        SSMLConst.AUDIO_OPEN_TAG, epubFile.profanityMap.get(word), SSMLConst.AUDIO_CLOSE_TAG) : word)
                .map(word -> epubFile.substituteMap.keySet().contains(word) ? String.format("%s\"%s\">%s%s",
                        SSMLConst.SUB_OPEN_TAG, epubFile.substituteMap.get(word), word, SSMLConst.SUB_CLOSE_TAG) : word)
                .map(word -> epubFile.characterList.contains(word) ? String.format("%s%s%s",
                        SSMLConst.SAY_AS_OPEN_TAG, word, SSMLConst.SAY_AS_CLOSE_TAG) : word)
                .collect(Collectors.toList());

        censoredWords.stream().forEach(System.out::println);

        return String.format("%s%s%s", SSMLConst.SPEAK_OPEN_TAG,
                StringUtils.join(censoredWords, " "), SSMLConst.SPEAK_CLOSE_TAG);
    }

}
