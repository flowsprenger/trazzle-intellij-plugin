package com.wooga.intellij.trazzle;

import com.intellij.openapi.project.Project;
import com.wooga.trazzle.intellij.ITrazzleClientListener;
import com.wooga.trazzle.intellij.TrazzleClient;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class TrazzleConsoleClient implements ITrazzleClientListener {

    private HashMap<Integer,TrazzleConsoleClientWindow> consoles = new HashMap<Integer, TrazzleConsoleClientWindow>();

    private TrazzleConsoleWindow content;

    private  JTabbedPane tabbedPane;
    private Project project;

    public TrazzleConsoleClient(TrazzleConsoleWindow content, Project project) {
        new TrazzleClient(this);
        this.content = content;
        this.project = project;

        tabbedPane = (JTabbedPane) content.getRootPanel().getComponent(0);
        tabbedPane.remove(0);
    }

    public void log(Integer clientId, String message, String level, String stacktrace, Number timestamp, Integer stackindex) {
        if(clientId == -1)
        {
            System.out.println(message);
        }else{
            TrazzleConsoleClientWindow console = consoles.get(clientId);
            if(console != null)
            {
                console.addElement(new RemoteLogElement(message, level, stacktrace, timestamp, stackindex));
            }else{
                System.out.println(message);
            }
        }
    }

    public void addClient(Integer clientId) {


        TrazzleConsoleClientWindow panel = new TrazzleConsoleClientWindow(clientId, project);
        tabbedPane.add(panel);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);
        consoles.put(clientId, panel);
    }

    public void removeClient(Integer clientId) {
        TrazzleConsoleClientWindow console = consoles.get(clientId);
        if(console!=null)
        {
            Integer i = 0;
            while(i<tabbedPane.getTabCount())
            {
                Component c = tabbedPane.getComponentAt(i);
                Component d = console.getParent();
                if(tabbedPane.getComponentAt(i)==console)
                {
                    tabbedPane.remove(i);
                    consoles.remove(clientId);
                }
                i++;
            }
        }
    }
}
