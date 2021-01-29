package com.example.slur.censor.utils.interfaces;

@FunctionalInterface
public interface ExceptionLog {

    public String generateLog(String exception, String paramName, String paramValue, String exceptionMessage);

}
