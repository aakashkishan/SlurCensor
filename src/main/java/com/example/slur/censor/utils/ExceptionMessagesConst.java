package com.example.slur.censor.utils;

public class ExceptionMessagesConst {

    public final static String EPUB_READER_NOT_FOUND_EXCEPTION_MESSAGE = "epub reader not found!";

    public final static String EPUB_BOOK_SECTION_NOT_FOUND_EXCEPTION_MESSAGE = "epub book section not found!";

    public final static String EPUB_BOOK_SECTION_OUT_OF_PAGES_EXCEPTION_MESSAGE = "epub book section out of pages!";

    public final static String CSV_FILE_NOT_FOUND_EXCEPTION_MESSAGE = "profanity CSV file not found!";

    public final static String CSV_VALIDATION_EXCEPTION_MESSAGE = "profanity CSV is invalid!";

    public final static String IO_EXCEPTION_MESSAGE = "IO Exception / Profanity CSV could not be access properly!";

    public final static String TXT_FILE_NOT_FOUND_EXCEPTION_MESSAGE = "Txt file not found!";

    public final static String FOLDER_NOT_FOUND_EXCEPTION = "Folder not found!";

    public final static String EPUB_FILE_NOT_FOUND = "Epub file not found!";

    public static String generateEpubReaderNotFoundExceptionMessage(String fileName) {
        return String.format("%s: FileName=%s, Message=%s", ExceptionsConst.EPUB_READER_NOT_FOUND_EXCEPTION,
                fileName, EPUB_READER_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    public static String generateEpubBookSectionNotFoundExceptionMessage(int index) {
        return String.format("%s: PageNumber=%s, Message=%s", ExceptionsConst.EPUB_BOOK_SECTION_NOT_FOUND_EXCEPTION,
                index, EPUB_BOOK_SECTION_NOT_FOUND_EXCEPTION_MESSAGE);
    }

    public static String generateEpubBookSectionOutOfPagesExceptionMessage(int index) {
        return String.format("%s: PageNumber=%s, Message=%s", ExceptionsConst.EPUB_BOOK_SECTION_OUT_OF_PAGES_EXCEPTION,
                index, EPUB_BOOK_SECTION_OUT_OF_PAGES_EXCEPTION_MESSAGE);
    }

}
