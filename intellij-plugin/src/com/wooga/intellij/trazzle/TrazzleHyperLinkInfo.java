package com.wooga.intellij.trazzle;

import com.intellij.execution.filters.HyperlinkInfo;
import com.intellij.ide.OpenFileXmlRpcHandler;
import com.intellij.openapi.project.Project;

public class TrazzleHyperLinkInfo implements HyperlinkInfo {

    private String file;
    private String line;

    public TrazzleHyperLinkInfo(String file, String line) {
        this.file = file;
        this.line = line;
    }

    public void navigate(Project project) {
        OpenFileXmlRpcHandler fileHandler = new OpenFileXmlRpcHandler();
        fileHandler.openAndNavigate(file, Integer.parseInt(line)-1, 0);
    }
}
