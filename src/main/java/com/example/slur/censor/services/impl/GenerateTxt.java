package com.example.slur.censor.services.impl;

import com.example.slur.censor.model.EpubFile;
import com.example.slur.censor.services.IGenerateTxt;
import com.example.slur.censor.utils.*;
import com.example.slur.censor.utils.interfaces.ExceptionLog;
import com.example.slur.censor.utils.interfaces.InfoLog;
import com.github.mertakdut.BookSection;
import com.github.mertakdut.Reader;
import com.github.mertakdut.exception.OutOfPagesException;
import com.github.mertakdut.exception.ReadingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Slf4j
@Service
public class GenerateTxt implements IGenerateTxt<BookSection, EpubFile> {

    Reader reader;

    BookSection bookSection;

    ExceptionLog exceptionLog = (exception, paramName, paramValue, exceptionMessage) ->
            String.format("Exception=%s: Message=%s", exception, exceptionMessage);

    InfoLog infoLog = (event, message) -> String.format("Event=%s: Message=%s", event, message);

    public String getBasePath(String fileName) {
        return String.format("%s/%s", DirectoryPathConst.BOOKS_BASE_PATH, fileName);
    }

    /**
     * Get the Text File Path
     * @param fileName
     * @return Txt File Path
     */
    public String getTxtFilePath(String fileName) {
        return String.format("%s/%s%s", getBasePath(fileName), fileName, DirectoryPathConst.TXT_EXTENSION);
    }

    public boolean getTxtFile(String fileName) {
        try {
            File txtFileObj = new File(getTxtFilePath(fileName));
            if(txtFileObj.exists()) {
                return true;
            } else if(txtFileObj.createNewFile()) {
                log.info(infoLog.generateLog(EventConst.TXT_FILE_CREATE, InfoMessageConst.TXT_FILE_CREATE_SUCCESS));
                return true;
            } else {
                throw new IOException(InfoMessageConst.TXT_FILE_CREATE_FAILURE);
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

    public boolean writeRefactoredTextToTxtFile(List<String> refactoredText, EpubFile epubFile) {

        boolean[] writeSuccess = {true};
        if(getTxtFile(epubFile.getFileName())) {
            try {
                FileWriter txtFileWriter = new FileWriter(getTxtFilePath(epubFile.getFileName()), false);
                log.info(infoLog.generateLog(EventConst.TXT_FILE_WRITER_INITIALIZE, InfoMessageConst.TXT_FILE_WRITER_INITIALIZE_SUCCESS));

                refactoredText.stream().forEach(text -> {
                    try {
                        txtFileWriter.write(text);
                        log.info(infoLog.generateLog(EventConst.TXT_FILE_WRITE, InfoMessageConst.TXT_FILE_WRITE_SUCCESS));
                    } catch (IOException e) {
                        log.error(infoLog.generateLog(EventConst.TXT_FILE_WRITE, InfoMessageConst.TXT_FILE_WRITE_FAILURE));
                        writeSuccess[0] = false;
                    }
                });
                txtFileWriter.close();
            } catch (IOException ioexp) {
                log.error(infoLog.generateLog(EventConst.TXT_FILE_WRITER_INITIALIZE, InfoMessageConst.TXT_FILE_WRITER_INITIALIZE_FAILURE));
                writeSuccess[0] = false;
            }
        }

        return writeSuccess[0];
    }

}
