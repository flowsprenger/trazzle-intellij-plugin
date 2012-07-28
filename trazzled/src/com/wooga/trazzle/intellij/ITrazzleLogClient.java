package com.wooga.trazzle.intellij;

public interface ITrazzleLogClient {
    void log(Integer clientId, String message, String level, String stacktrace, Number timestamp, Integer stackindex);
    void addClient(Integer clientId);
    void removeClient(Integer clientId);
}
