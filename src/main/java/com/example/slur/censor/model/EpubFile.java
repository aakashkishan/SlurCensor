package com.example.slur.censor.model;

import com.example.slur.censor.utils.*;
import com.example.slur.censor.utils.interfaces.ExceptionLog;
import com.example.slur.censor.utils.interfaces.InfoLog;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
public class EpubFile {

    ExceptionLog exceptionLog = (exception, paramName, paramValue, exceptionMessage) ->
            String.format("Exception=%s: Message=%s", exception, exceptionMessage);
    InfoLog infoLog = (event, message) -> String.format("Event=%s: Message=%s", event, message);

    private String fileName;

    public HashMap<String, String> profanityMap = new HashMap<>();

    public HashMap<String, String> substituteMap = new HashMap<>();

    public ArrayList<String> characterList = new ArrayList<>();

    public EpubFile(String file) {
        this.fileName = file;
        loadProfanityMap();
        loadSubstituteMap();
        loadCharacterList();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void addProfanityEntry(String profanity, String replaceString) {
        this.profanityMap.put(profanity, replaceString);
    }

    public void removeProfanityEntry(String profanity) {
        this.profanityMap.remove(profanity);
    }

    public void addSubstituteEntry(String acronym, String substitute) {
        this.substituteMap.put(acronym, substitute);
    }

    public void removeSubstituteEntry(String acronym) {
        this.substituteMap.remove(acronym);
    }

    public void addCharacterElement(String element) {
        this.characterList.add(element);
    }

    public void removeCharacterElement(String element) {
        this.characterList.remove(element);
    }

    private void loadProfanityMap() {
        File file = new File(String.format("%s/%s%s", getBookBaseUrl(),
                DirectoryPathConst.PROFANITY_FILE, DirectoryPathConst.CSV_EXTENSION));
        if(file.exists()) {
            try (CSVReader reader = new CSVReader(new FileReader(String.format("%s/%s%s", getBookBaseUrl(),
                    DirectoryPathConst.PROFANITY_FILE, DirectoryPathConst.CSV_EXTENSION)))) {
                String[] lineInArray;
                while ((lineInArray = reader.readNext()) != null) {
                    profanityMap.put(lineInArray[0], lineInArray[1]);
                }
            } catch (FileNotFoundException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_FILE_NOT_FOUND_EXCEPTION, null,
                        null, ExceptionMessagesConst.CSV_FILE_NOT_FOUND_EXCEPTION_MESSAGE));
            } catch (IOException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, null,
                        null, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            } catch (CsvValidationException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_VALIDATION_EXCEPTION, null,
                        null, ExceptionMessagesConst.CSV_VALIDATION_EXCEPTION_MESSAGE));
            }
        } else {
            try {
                if(file.createNewFile()) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_SUCCESS));
                } else {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_FAILURE));
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, null,
                        null, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            }
        }
    }

    public boolean writeProfanityMap() {
        boolean writeSuccess = true;
        File file = new File(String.format("%s/%s%s", getBookBaseUrl(),
                DirectoryPathConst.PROFANITY_FILE, DirectoryPathConst.CSV_EXTENSION));

        if(file.exists()) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("%s/%s%s", getBookBaseUrl(),
                    DirectoryPathConst.PROFANITY_FILE, DirectoryPathConst.CSV_EXTENSION)))) {

                try(CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_WRITER_INITIALIZE, InfoMessageConst.CSV_FILE_WRITER_INITIALIZE_SUCCESS));
                    for(Map.Entry<String, String> entry: this.profanityMap.entrySet()) {
                        csvPrinter.printRecord(Arrays.asList(entry.getKey(), entry.getValue()));
                    }
                    csvPrinter.flush();
                } catch (IOException ioexp) {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_WRITER_INITIALIZE, InfoMessageConst.CSV_FILE_WRITER_INITIALIZE_FAILURE));
                    writeSuccess = false;
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_FILE_NOT_FOUND_EXCEPTION,
                        null, null, ExceptionMessagesConst.CSV_FILE_NOT_FOUND_EXCEPTION_MESSAGE));
                writeSuccess = false;
            }
        } else {
            try {
                if(file.createNewFile()) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_SUCCESS));
                } else {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_FAILURE));
                    writeSuccess = false;
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                        DirectoryPathConst.PROFANITY_FILE, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
                writeSuccess = false;
            }
        }
        return  writeSuccess;
    }

    private void loadSubstituteMap() {
        File file = new File(String.format("%s/%s%s", getBookBaseUrl(),
                DirectoryPathConst.SUBSTITUTE_FILE, DirectoryPathConst.CSV_EXTENSION));
        if(file.exists()) {
            try (CSVReader reader = new CSVReader(new FileReader(String.format("%s/%s%s", getBookBaseUrl(),
                    DirectoryPathConst.SUBSTITUTE_FILE, DirectoryPathConst.CSV_EXTENSION)))) {
                String[] lineInArray;
                while ((lineInArray = reader.readNext()) != null) {
                    this.substituteMap.put(lineInArray[0], lineInArray[1]);
                }
            } catch (FileNotFoundException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_FILE_NOT_FOUND_EXCEPTION, null,
                        null, ExceptionMessagesConst.CSV_FILE_NOT_FOUND_EXCEPTION_MESSAGE));
            } catch (IOException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, null,
                        null, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            } catch (CsvValidationException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_VALIDATION_EXCEPTION, null,
                        null, ExceptionMessagesConst.CSV_VALIDATION_EXCEPTION_MESSAGE));
            }
        } else {
            try {
                if(file.createNewFile()) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_SUCCESS));
                } else {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_FAILURE));
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, null,
                        null, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            }
        }
    }

    public boolean writeSubstituteMap() {
        boolean writeSuccess = true;
        File file = new File(String.format("%s/%s%s", getBookBaseUrl(),
                DirectoryPathConst.SUBSTITUTE_FILE, DirectoryPathConst.CSV_EXTENSION));

        if(file.exists()) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("%s/%s%s", getBookBaseUrl(),
                    DirectoryPathConst.SUBSTITUTE_FILE, DirectoryPathConst.CSV_EXTENSION)))) {

                try(CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_WRITER_INITIALIZE, InfoMessageConst.CSV_FILE_WRITER_INITIALIZE_SUCCESS));
                    for(Map.Entry<String, String> entry: this.substituteMap.entrySet()) {
                        csvPrinter.printRecord(Arrays.asList(entry.getKey(), entry.getValue()));
                    }
                    csvPrinter.flush();
                } catch (IOException ioexp) {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_WRITER_INITIALIZE, InfoMessageConst.CSV_FILE_WRITER_INITIALIZE_FAILURE));
                    writeSuccess = false;
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_FILE_NOT_FOUND_EXCEPTION,
                        null, null, ExceptionMessagesConst.CSV_FILE_NOT_FOUND_EXCEPTION_MESSAGE));
                writeSuccess = false;
            }
        } else {
            try {
                if(file.createNewFile()) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_SUCCESS));
                } else {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_FAILURE));
                    writeSuccess = false;
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                        DirectoryPathConst.SUBSTITUTE_FILE, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
                writeSuccess = false;
            }
        }
        return  writeSuccess;
    }

    private void loadCharacterList() {
        File file = new File(String.format("%s/%s%s", getBookBaseUrl(),
                DirectoryPathConst.CHARACTER_FILE, DirectoryPathConst.CSV_EXTENSION));
        if(file.exists()) {
            try (CSVReader reader = new CSVReader(new FileReader(String.format("%s/%s%s", getBookBaseUrl(),
                    DirectoryPathConst.CHARACTER_FILE, DirectoryPathConst.CSV_EXTENSION)))) {
                String[] lineInArray;
                if ((lineInArray = reader.readNext()) != null) {
                    this.characterList.addAll(Arrays.asList(lineInArray));
                }
            } catch (FileNotFoundException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_FILE_NOT_FOUND_EXCEPTION, null,
                        null, ExceptionMessagesConst.CSV_FILE_NOT_FOUND_EXCEPTION_MESSAGE));
            } catch (IOException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, null,
                        null, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            } catch (CsvValidationException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_VALIDATION_EXCEPTION, null,
                        null, ExceptionMessagesConst.CSV_VALIDATION_EXCEPTION_MESSAGE));
            }
        } else {
            try {
                if(file.createNewFile()) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_SUCCESS));
                } else {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_FAILURE));
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, null,
                        null, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            }
        }
    }

    public boolean writeCharacterList() {
        boolean writeSuccess = true;
        File file = new File(String.format("%s/%s%s", getBookBaseUrl(),
                DirectoryPathConst.CHARACTER_FILE, DirectoryPathConst.CSV_EXTENSION));

        if(file.exists()) {
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(String.format("%s/%s%s", getBookBaseUrl(),
                    DirectoryPathConst.CHARACTER_FILE, DirectoryPathConst.CSV_EXTENSION)))) {

                try(CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_WRITER_INITIALIZE, InfoMessageConst.CSV_FILE_WRITER_INITIALIZE_SUCCESS));
                    csvPrinter.printRecord(this.characterList);
                    csvPrinter.flush();
                } catch (IOException ioexp) {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_WRITER_INITIALIZE, InfoMessageConst.CSV_FILE_WRITER_INITIALIZE_FAILURE));
                    writeSuccess = false;
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.CSV_FILE_NOT_FOUND_EXCEPTION,
                        null, null, ExceptionMessagesConst.CSV_FILE_NOT_FOUND_EXCEPTION_MESSAGE));
                writeSuccess = false;
            }
        } else {
            try {
                if(file.createNewFile()) {
                    log.info(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_SUCCESS));
                } else {
                    log.error(infoLog.generateLog(EventConst.CSV_FILE_CREATE, InfoMessageConst.CSV_FILE_CREATE_FAILURE));
                    writeSuccess = false;
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                        DirectoryPathConst.CHARACTER_FILE, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
                writeSuccess = false;
            }
        }
        return  writeSuccess;
    }

    private String getBookBaseUrl() {
        return String.format("%s/%s", DirectoryPathConst.BOOKS_BASE_PATH, fileName);
    }

}
