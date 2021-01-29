package com.example.slur.censor.services.impl;

import com.example.slur.censor.model.EpubFile;
import com.example.slur.censor.services.IGenerateSSML;
import com.example.slur.censor.utils.*;
import com.example.slur.censor.utils.interfaces.ExceptionLog;
import com.example.slur.censor.utils.interfaces.InfoLog;
import com.github.mertakdut.BookSection;
import com.github.mertakdut.Reader;
import com.github.mertakdut.exception.OutOfPagesException;
import com.github.mertakdut.exception.ReadingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GenerateSSML implements IGenerateSSML<BookSection, EpubFile> {

    Reader reader;

    BookSection bookSection;

    ExceptionLog exceptionLog = (exception, paramName, paramValue, exceptionMessage) ->
            String.format("Exception=%s: Message=%s", exception, exceptionMessage);

    InfoLog infoLog = (event, message) -> String.format("Event=%s: Message=%s", event, message);

    public String getBasePath(String fileName) {
        return String.format("%s/%s", DirectoryPathConst.BOOKS_BASE_PATH, fileName);
    }

    /**
     * Get the SSML File Path
     * @param fileName
     * @return SSML File Path
     */
    public String getSSMLFilePath(String fileName) {
        return String.format("%s/%s%s", getBasePath(fileName), fileName, DirectoryPathConst.SSML_EXTENSION);
    }

    public boolean getSSMLFile(String fileName) {
        try {
            File ssmlFileObj = new File(getSSMLFilePath(fileName));
            if(ssmlFileObj.exists()) {
                return true;
            } else if(ssmlFileObj.createNewFile()) {
                log.info(infoLog.generateLog(EventConst.SSML_FILE_CREATE, InfoMessageConst.SSML_FILE_CREATE_SUCCESS));
                return true;
            } else {
                throw new IOException(InfoMessageConst.SSML_FILE_CREATE_FAILURE);
            }
        } catch (IOException ioexp) {
            log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                    fileName, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            return false;
        }
    }

    public List<BookSection> readEpubIntoBookSections(EpubFile epubFile) {
        String filePath = String.format("%s/%s%s", getBasePath(epubFile.getFileName()),
                epubFile.getFileName(), DirectoryPathConst.EPUB_EXTENSION);
        List<BookSection> bookSections = new ArrayList<>();
        int index = 0;
        boolean failedBookSection = false;
        ExceptionLog exceptionLog = (exception, paramName, paramValue, exceptionMessage) ->
                String.format("Exception=%s: %s=%s, Message=%s", exception, paramName, paramValue, exceptionMessage);

        // Reader Object Parameters Set
        reader = new Reader();
        reader.setMaxContentPerSection(1000);
        reader.setIsIncludingTextContent(true);
        try {
            reader.setFullContent(filePath);
        } catch (ReadingException rexp) {
            log.error(exceptionLog.generateLog(ExceptionsConst.EPUB_READER_NOT_FOUND_EXCEPTION, "fileName",
                    epubFile.getFileName(), ExceptionMessagesConst.EPUB_READER_NOT_FOUND_EXCEPTION_MESSAGE));
        }

        while(!failedBookSection) {
            try {
                bookSection = reader.readSection(index);
                System.out.println(bookSection.getSectionTextContent());
                bookSections.add(bookSection);
            } catch (OutOfPagesException oopexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.EPUB_BOOK_SECTION_OUT_OF_PAGES_EXCEPTION, "pageNumber",
                        String.valueOf(index), ExceptionMessagesConst.EPUB_BOOK_SECTION_OUT_OF_PAGES_EXCEPTION_MESSAGE));
                failedBookSection = true;
            } catch (ReadingException rexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.EPUB_BOOK_SECTION_NOT_FOUND_EXCEPTION, "pageNumber",
                        String.valueOf(index), ExceptionMessagesConst.EPUB_BOOK_SECTION_NOT_FOUND_EXCEPTION_MESSAGE));
                failedBookSection = true;
            }
            index += 1;
        }

        return bookSections;
    }

    public boolean writeRefactoredTextToSSMLFile(List<String> refactoredText, EpubFile epubFile) {

        boolean[] writeSuccess = {true};
        if(getSSMLFile(epubFile.getFileName())) {
            try {
                FileWriter ssmlFileWriter = new FileWriter(getSSMLFilePath(epubFile.getFileName()), false);
                log.info(infoLog.generateLog(EventConst.SSML_FILE_WRITER_INITIALIZE, InfoMessageConst.SSML_FILE_WRITER_INITIALIZE_SUCCESS));

                refactoredText.stream().forEach(text -> {
                    try {
                        ssmlFileWriter.write(text);
                        log.info(infoLog.generateLog(EventConst.SSML_FILE_WRITE, InfoMessageConst.SSML_FILE_WRITE_SUCCESS));
                    } catch (IOException e) {
                        log.error(infoLog.generateLog(EventConst.SSML_FILE_WRITE, InfoMessageConst.SSML_FILE_WRITE_FAILURE));
                        writeSuccess[0] = false;
                    }
                });
                ssmlFileWriter.close();
            } catch (IOException ioexp) {
                log.error(infoLog.generateLog(EventConst.SSML_FILE_WRITER_INITIALIZE, InfoMessageConst.SSML_FILE_WRITER_INITIALIZE_FAILURE));
                writeSuccess[0] = false;
            }
        }

        return writeSuccess[0];
    }

}
