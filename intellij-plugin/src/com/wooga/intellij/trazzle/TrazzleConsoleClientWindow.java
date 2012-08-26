package com.wooga.intellij.trazzle;

import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class TrazzleConsoleClientWindow extends JPanel implements ActionListener{

    private Vector<RemoteLogElement> allMessages  = new Vector<RemoteLogElement>();
    private String currentFilter = null;
    private Project project;
    final private ConsoleViewImpl console;

    public TrazzleConsoleClientWindow(Integer clientId, final Project project) {
        this.project = project;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setName(clientId.toString());

        JTextField textField = new JTextField("",50);
        textField.setMaximumSize( textField.getPreferredSize() );
        textField.addActionListener(this);
        add(textField);

        console = new ConsoleViewImpl(project, true);
        console.setSize(100,100);
        Runnable doWorkRunnable = new Runnable() {
            public void run() {
                add(console.getComponent());
            }
        };
        SwingUtilities.invokeLater(doWorkRunnable);
    }

    public void addElement(RemoteLogElement remoteLogElement) {

        allMessages.add(remoteLogElement);
        applyFilterAndAdd(remoteLogElement);
    }

    private void applyFilterAndAdd(RemoteLogElement remoteLogElement) {
        if(currentFilter != null)
        {
            if(remoteLogElement.toString().matches(currentFilter))
            {
                addLogMessageToConsole(remoteLogElement);
            }
        }else{
            addLogMessageToConsole(remoteLogElement);
        }
    }

    private void addLogMessageToConsole(RemoteLogElement remoteLogElement) {
        console.print(remoteLogElement.toString() + " (", errorLevelToConsoleViewContentType(remoteLogElement.level));
        if(remoteLogElement.stacktrace!=null)
        {
            String stackElement = remoteLogElement.stacktrace.split("\n")[remoteLogElement.stackindex+1];
            String fileAndLine = stackElement.substring(stackElement.indexOf('[')+1,stackElement.indexOf(']'));
            String line = fileAndLine.substring(fileAndLine.lastIndexOf(":")+1);
            String file = fileAndLine.substring(0, fileAndLine.lastIndexOf(":"));
            console.printHyperlink(file+":"+line, new TrazzleHyperLinkInfo(file, line));
        }
        console.print(")\n", ConsoleViewContentType.NORMAL_OUTPUT);
        console.scrollToEnd();
    }

    private ConsoleViewContentType errorLevelToConsoleViewContentType(String level) {
        switch(level.getBytes()[0])
        {
            case 'e': // error
            case 'f': // fatal
            case 'c': // critical
                return ConsoleViewContentType.ERROR_OUTPUT;
            case 'n': // notice
            case 'w':  // warning
                return ConsoleViewContentType.USER_INPUT;
            case 'i':  // info
            case 'd':  // debug
                return ConsoleViewContentType.NORMAL_OUTPUT;
        }
        return ConsoleViewContentType.NORMAL_OUTPUT;
    }

    public void actionPerformed(ActionEvent e) {
        filter(e.getActionCommand());
    }

    public void filter(String prefix) {
        console.clear();
        if(prefix=="")
        {
            currentFilter = null;
        }else{
            currentFilter = ".*"+prefix+".*";
        }

        for( int i=0; i<allMessages.size(); i++) {
            applyFilterAndAdd(allMessages.elementAt(i));
        }
    }
}
