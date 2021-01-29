package com.example.slur.censor.cli;

import com.example.slur.censor.model.EpubFile;
import com.example.slur.censor.services.IGenerateSSML;
import com.example.slur.censor.services.IGenerateTxt;
import com.example.slur.censor.services.IRefactorEpub;
import com.example.slur.censor.services.ITextToSpeech;
import com.example.slur.censor.utils.*;
import com.example.slur.censor.utils.interfaces.ExceptionLog;
import com.example.slur.censor.utils.interfaces.InfoLog;
import com.github.mertakdut.BookSection;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@ShellComponent
@Slf4j
public class ProfanityCensorShell {

    ExceptionLog exceptionLog = (exception, paramName, paramValue, exceptionMessage) ->
            String.format("Exception=%s: Message=%s", exception, exceptionMessage);

    InfoLog infoLog = (event, message) -> String.format("Event=%s: Message=%s", event, message);

    private final IGenerateSSML<BookSection, EpubFile> generateSSML;

    private final IGenerateTxt<BookSection, EpubFile> generateTxt;

    private final IRefactorEpub<BookSection, EpubFile> refactorEpub;

    private final ITextToSpeech<EpubFile> textToSpeech;

    public ProfanityCensorShell(IGenerateTxt<BookSection, EpubFile> generateTxt, IGenerateSSML<BookSection, EpubFile> generateSSML,
                                IRefactorEpub<BookSection, EpubFile> refactorEpub, ITextToSpeech<EpubFile> textToSpeech) {
        this.generateTxt = generateTxt;
        this.generateSSML = generateSSML;
        this.refactorEpub = refactorEpub;
        this.textToSpeech = textToSpeech;
    }

    public String getBasePath(String fileName) {
        return String.format("%s/%s", DirectoryPathConst.BOOKS_BASE_PATH, fileName);
    }

    public boolean createBookFolder(String fileName) {
        boolean isExists = true;

        File file = new File(getBasePath(fileName));
        if(!file.exists()) {
            if(file.mkdirs()) {
                log.info(infoLog.generateLog(EventConst.FOLDER_CREATE, InfoMessageConst.FOLDER_CREATE_SUCCESS));
            } else {
                log.error(infoLog.generateLog(EventConst.FOLDER_CREATE, InfoMessageConst.FOLDER_CREATE_FAILURE));
                isExists = false;
            }
        }
        return isExists;
    }

    public boolean copyFileToBookFolder(String fileName, String filePath) {
        boolean isCopied = false;

        File sourceFile = new File(filePath);
        File destinationFile = new File(String.format("%s/%s%s", getBasePath(fileName),
                fileName, DirectoryPathConst.EPUB_EXTENSION));
        if(sourceFile.exists()) {
            try {
                if(destinationFile.createNewFile()) {
                    log.info(infoLog.generateLog(EventConst.EPUB_FILE_CREATE, InfoMessageConst.EPUB_FILE_CREATE_SUCCESS));
                    FileUtils.copyFile(sourceFile, destinationFile);
                    isCopied = true;
                } else {
                    log.error(infoLog.generateLog(EventConst.EPUB_FILE_CREATE, InfoMessageConst.EPUB_FILE_CREATE_FAILURE));
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                        fileName, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            }
        } else {
            log.error(exceptionLog.generateLog(ExceptionsConst.EPUB_FILE_NOT_FOUND, null,
                    null, ExceptionMessagesConst.EPUB_FILE_NOT_FOUND));
        }
        return isCopied;
    }

    @ShellMethod(key="sc-setup", value="Slur Censor Setup")
    public boolean setup(@ShellOption("--filename") String fileName, @ShellOption("--filepath") String filePath) {
        boolean isSetup = false;
        if(createBookFolder(fileName) && copyFileToBookFolder(fileName, filePath)) {
            EpubFile epubFile = new EpubFile(fileName);
            if(epubFile.writeProfanityMap() && epubFile.writeSubstituteMap() && epubFile.writeCharacterList()) {
                isSetup = true;
            }
        }
        return isSetup;
    }

    @ShellMethod(key="sc-add-profanity", value="Add Profanity Filter to the Slur Censor")
    public boolean addProfanity(@ShellOption("--filename") String fileName,
                                @ShellOption("--profanity") String profanity, @ShellOption("--replace") String replace) {
        boolean isAdded = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);
            epubFile.addProfanityEntry(profanity, replace);
            if(epubFile.writeProfanityMap()) {
                isAdded = true;
            }
        }
        return isAdded;
    }

    @ShellMethod(key="sc-add-substitute", value="Add Substitute Filter to the Slur Censor")
    public boolean addSubstitute(@ShellOption("--filename") String fileName,
                                @ShellOption("--acronym") String acronym, @ShellOption("--substitute") String substitute) {
        boolean isAdded = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);
            epubFile.addSubstituteEntry(acronym, substitute);
            if(epubFile.writeSubstituteMap()) {
                isAdded = true;
            }
        }
        return isAdded;
    }

    @ShellMethod(key="sc-remove-profanity", value="Remove profanity filter from the Slur Censor")
    public boolean removeProfanity(@ShellOption("--filename") String fileName,
                                   @ShellOption("--profanity") String profanity) {
        boolean isRemoved = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);
            epubFile.removeProfanityEntry(profanity);
            if(epubFile.writeProfanityMap()) {
                isRemoved = true;
            }
        }
        return isRemoved;
    }

    @ShellMethod(key="sc-remove-substitute", value="Remove substitute filter from the Slur Censor")
    public boolean removeSubstitute(@ShellOption("--filename") String fileName,
                                   @ShellOption("--acronym") String acronym) {
        boolean isRemoved = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);
            epubFile.removeSubstituteEntry(acronym);
            if(epubFile.writeSubstituteMap()) {
                isRemoved = true;
            }
        }
        return isRemoved;
    }

    @ShellMethod(key="sc-add-character", value="Add Character Filter to the Slur Censor")
    public boolean addCharacter(@ShellOption("--filename") String fileName,
                                @ShellOption("--character") String characterString) {
        boolean isAdded = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);
            log.info(String.format("CharacterString=%s", characterString));
            epubFile.addCharacterElement(characterString);
            if(epubFile.writeCharacterList()) {
                isAdded = true;
            }
        }
        return isAdded;
    }

    @ShellMethod(key="sc-remove-character", value="Remove character filter from the Slur Censor")
    public boolean removeCharacter(@ShellOption("--filename") String fileName,
                                   @ShellOption("--character") String characterString) {
        boolean isRemoved = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);
            epubFile.removeCharacterElement(characterString);
            if(epubFile.writeCharacterList()) {
                isRemoved = true;
            }
        }
        return isRemoved;
    }

    @ShellMethod(key="sc-generate-txt", value="Generate txt file for the Epub file with the requested refactors")
    public boolean generateTxtFile(@ShellOption("--filename") String fileName) {
        boolean isGenerated = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);

            List<BookSection> bookSections = this.generateTxt.readEpubIntoBookSections(epubFile);
            List<String> refactoredTexts = bookSections.stream().map(bookSection ->
                    this.refactorEpub.refactorEpubWithProfanityAndSubstitutionForTxt(bookSection, epubFile))
                    .collect(Collectors.toList());
            if(this.generateTxt.writeRefactoredTextToTxtFile(refactoredTexts, epubFile)) {
                isGenerated = true;
            }
        }
        return isGenerated;
    }

    @ShellMethod(key="sc-generate-ssml", value="Generate SSML file for the Epub file with the requested refactors")
    public boolean generateSSMLFile(@ShellOption("--filename") String fileName) {
        boolean isGenerated = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);

            List<BookSection> bookSections = this.generateSSML.readEpubIntoBookSections(epubFile);
            List<String> refactoredTexts = bookSections.stream().map(bookSection ->
                    this.refactorEpub.refactorEpubWithProfanityAndSubstitutionForSSML(bookSection, epubFile))
                    .collect(Collectors.toList());
            if(this.generateSSML.writeRefactoredTextToSSMLFile(refactoredTexts, epubFile)) {
                isGenerated = true;
            }
        }
        return isGenerated;
    }

    @ShellMethod(key="sc-generate-mp3", value="Generate Audio file for the Epub file with the requested refactors")
    public boolean generateMp3File(@ShellOption("--filename") String fileName) {
        boolean isGenerated = false;
        if(createBookFolder(fileName)) {
            EpubFile epubFile = new EpubFile(fileName);

            List<BookSection> bookSections = this.generateSSML.readEpubIntoBookSections(epubFile);
            List<String> refactoredTexts = bookSections.stream().map(bookSection ->
                    this.refactorEpub.refactorEpubWithProfanityAndSubstitutionForSSML(bookSection, epubFile))
                    .collect(Collectors.toList());
            if(this.generateSSML.writeRefactoredTextToSSMLFile(refactoredTexts, epubFile)) {
                if(textToSpeech.synthesizeSSML(epubFile)) {
                    isGenerated = true;
                }
            }
        }
        return isGenerated;
    }


}
