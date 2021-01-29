package com.example.slur.censor.services.impl;

import com.example.slur.censor.exceptions.FileNotFoundException;
import com.example.slur.censor.exceptions.TextToSpeechException;
import com.example.slur.censor.model.EpubFile;
import com.example.slur.censor.services.ITextToSpeech;
import com.example.slur.censor.utils.*;
import com.example.slur.censor.utils.interfaces.ExceptionLog;
import com.example.slur.censor.utils.interfaces.InfoLog;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class TextToSpeech implements ITextToSpeech<EpubFile> {

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

    /**
     * Get the MP3 File Path
     * @param fileName
     * @return MP3 File Path
     */
    public String getMP3FilePath(String fileName) {
        return String.format("%s/%s%s", getBasePath(fileName), fileName, DirectoryPathConst.MP3_EXTENSION);
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

    public boolean getMP3File(String fileName) {
        try {
            File mp3FileObj = new File(getMP3FilePath(fileName));
            if(mp3FileObj.exists()) {
                return true;
            } else if(mp3FileObj.createNewFile()) {
                log.info(infoLog.generateLog(EventConst.MP3_FILE_CREATE, InfoMessageConst.MP3_FILE_CREATE_SUCCESS));
                return true;
            } else {
                throw new IOException(InfoMessageConst.MP3_FILE_CREATE_FAILURE);
            }
        } catch (IOException ioexp) {
            log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                    fileName, ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            return false;
        }
    }

    public boolean synthesizeSSML(EpubFile epubFile) {
        boolean isAudio = false;
        List<String> ssmlLines = Collections.emptyList();
        if(getSSMLFile(epubFile.getFileName())) {
            try {
                ssmlLines = Files.readAllLines(Paths.get(getSSMLFilePath(epubFile.getFileName())), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                        String.format("%s%s", epubFile.getFileName(), DirectoryPathConst.SSML_EXTENSION),
                        ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
            }
            String ssmlContent = StringUtils.join(ssmlLines, "");

            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
                SynthesisInput input = SynthesisInput.newBuilder().setSsml(ssmlContent).build();
                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder().setLanguageCode("en-US")
                        .setSsmlGender(SsmlVoiceGender.MALE).build();

                AudioConfig audioConfig = AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();
                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

                ByteString audioContents = response.getAudioContent();
                if(getMP3File(epubFile.getFileName())) {
                    try (OutputStream outFileStream = new FileOutputStream(getMP3FilePath(epubFile.getFileName()))) {
                        outFileStream.write(audioContents.toByteArray());
                        log.info(infoLog.generateLog(EventConst.MP3_FILE_WRITE, InfoMessageConst.MP3_FILE_WRITE_SUCCESS));
                        isAudio = true;
                    } catch (IOException ioexp) {
                        log.info(infoLog.generateLog(EventConst.MP3_FILE_WRITE, InfoMessageConst.MP3_FILE_WRITE_FAILURE));
                    }
                }
            } catch (IOException ioexp) {
                log.error(exceptionLog.generateLog(ExceptionsConst.IO_EXCEPTION, "fileName",
                        String.format("%s%s", epubFile.getFileName(), DirectoryPathConst.MP3_EXTENSION),
                        ExceptionMessagesConst.IO_EXCEPTION_MESSAGE));
                throw new TextToSpeechException(ExceptionsConst.TEXT_TO_SPEECH_EXCEPTION, ioexp);
            }
        } else {
            throw new FileNotFoundException(String.format("Exception=%s: fileName=%s%s",
                    ExceptionsConst.FILE_NOT_FOUND_EXCEPTION, epubFile.getFileName(), DirectoryPathConst.SSML_EXTENSION));
        }
        return isAudio;
    }

}
