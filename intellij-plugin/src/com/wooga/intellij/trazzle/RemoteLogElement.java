package com.wooga.intellij.trazzle;

public class RemoteLogElement {

    private String message;

    public String level;

    public String stacktrace;
    public Number timestamp;
    public Integer stackindex;

    public RemoteLogElement(String message, String level, String stacktrace, Number timestamp, Integer stackindex) {
        this.message = message;
        this.level = level;
        this.stacktrace = stacktrace;
        this.timestamp = timestamp;
        this.stackindex = stackindex;
    }

    @Override
    public String toString() {
        return "[" + level + "] "+ message;
    }
}
