package com.wooga.trazzle.intellij;

/**
 * Created with IntelliJ IDEA.
 * User: flow
 * Date: 27.06.12
 * Time: 22:21
 * To change this template use File | Settings | File Templates.
 */
public class TrazzleClient implements ITrazzleLogClient {
    private ITrazzleClientListener client;

    public TrazzleClient() {
        initialize();
    }

    public TrazzleClient(ITrazzleClientListener trazzleConsoleClient) {
        client = trazzleConsoleClient;
        initialize();
    }

    public void initialize()
    {
        com.wooga.trazzle.intellij.TrazzleD.configureClient(this);
        com.wooga.trazzle.intellij.TrazzleD.start();
    }

    public void log(Integer clientId, String message, String level, String stacktrace, Number timestamp, Integer stackindex){
        client.log(clientId, message, level, stacktrace, timestamp, stackindex);
    }

    public void addClient(Integer clientId) {
        client.addClient(clientId);
    }

    public void removeClient(Integer clientId) {
        client.removeClient(clientId);
    }

    public void addSwfUrl(Integer clientId, String url) {
        client.addSwfUrl(clientId, url);
    }
}
