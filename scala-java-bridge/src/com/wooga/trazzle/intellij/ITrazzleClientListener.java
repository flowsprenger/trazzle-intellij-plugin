package com.wooga.trazzle.intellij;

/**
 * Created with IntelliJ IDEA.
 * User: flow
 * Date: 02.07.12
 * Time: 22:18
 * To change this template use File | Settings | File Templates.
 */
public interface ITrazzleClientListener {
    void log(Integer clientId, String message, String lebel, String stacktrace, Number timestamp, Integer stackindex);
    void addClient(Integer clientId);
    void removeClient(Integer clientId);
    void addSwfUrl(Integer clientId, String url);
}
