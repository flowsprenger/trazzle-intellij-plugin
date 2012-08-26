package com.wooga.intellij.trazzle;

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TrazzleConsole implements ProjectComponent {

    private Project project;
    private TrazzleConsoleWindow consoleWindow;

    public TrazzleConsole(Project project) {
        this.project = project;

    }

    public void projectOpened() {
        initConsoleWindow();
    }

    private void initConsoleWindow() {
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);

        consoleWindow = new TrazzleConsoleWindow();
        JPanel rootPanel = consoleWindow.getRootPanel();

        ToolWindow myToolWindow = toolWindowManager.registerToolWindow("Trazzle", false, ToolWindowAnchor.BOTTOM);
        Content content = ContentFactory.SERVICE.getInstance().createContent(rootPanel, "", false);

        myToolWindow.getContentManager().addContent(content);

        new TrazzleConsoleClient(consoleWindow, project);

    }

    public void projectClosed() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void initComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disposeComponent() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    public String getComponentName() {
        return "TrazzleConsole";
    }
}
