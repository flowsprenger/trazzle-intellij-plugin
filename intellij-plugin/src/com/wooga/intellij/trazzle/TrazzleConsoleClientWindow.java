package com.wooga.intellij.trazzle;

import com.intellij.ide.OpenFileXmlRpcHandler;
import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TrazzleConsoleClientWindow extends JPanel implements ActionListener{

    private JList<RemoteLogElement> list;
    private DefaultListModel listModel;
    private DefaultListModel filteredListModel;
    private String currentFilter = null;
    private Project project;

    public TrazzleConsoleClientWindow(Integer clientId, final Project project) {
        this.project = project;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setName(clientId.toString());

        JScrollPane jScrollPane = new JScrollPane();
        jScrollPane.setPreferredSize(new Dimension(392, 245));

        list = new JList<RemoteLogElement>();
        list.setCellRenderer(new TrazzleLogMessageCellRenderer());
        listModel = new DefaultListModel();
        list.setLayoutOrientation(JList.VERTICAL);
        list.setSize(new Dimension(500, 100));
        list.setModel(listModel);
        jScrollPane.setViewportView(list);

        JTextField textField = new JTextField("",50);
        textField.setMaximumSize( textField.getPreferredSize() );
        textField.addActionListener(this);
        add(textField);
        add(jScrollPane);

        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    if(index!=-1)
                    {
                        RemoteLogElement element = list.getModel().getElementAt(index);
                        System.out.println("Double clicked on Item " + element.stacktrace);
                        if(element.stacktrace!=null)
                        {
                            String stackElement = element.stacktrace.split("\n")[element.stackindex+1];
                            String fileAndLine = stackElement.substring(stackElement.indexOf('[')+1,stackElement.indexOf(']'));
                            String line = fileAndLine.substring(fileAndLine.lastIndexOf(":")+1);
                            String file = fileAndLine.substring(0, fileAndLine.lastIndexOf(":"));
                            OpenFileXmlRpcHandler fileHandler = new OpenFileXmlRpcHandler();
                            fileHandler.openAndNavigate(file, Integer.parseInt(line)-1, 0);
                        }
                    }
                }
                if(e.isControlDown())
                {
                    int index = list.locationToIndex(e.getPoint());
                    if(index!=-1)
                    {
                        RemoteLogElement element = list.getModel().getElementAt(index);
                    }
                }
            }
        };
        list.addMouseListener(mouseListener);
    }

    public void addElement(RemoteLogElement remoteLogElement) {
        listModel.addElement(remoteLogElement);
        if(currentFilter != null)
        {
            if(remoteLogElement.toString().matches(currentFilter))
            {
                filteredListModel.addElement(remoteLogElement);
                if(list.getSelectedIndex()==-1)
                {
                    list.ensureIndexIsVisible(filteredListModel.size() - 1);
                }
            }
        }else{
            if(list.getSelectedIndex()==-1)
            {
                list.ensureIndexIsVisible(listModel.size() - 1);
            }
        }

    }

    public void actionPerformed(ActionEvent e) {
        filter(e.getActionCommand());
    }

    public void filter(String prefix) {
        if(prefix=="")
        {
            currentFilter = null;
            list.setModel(listModel);
        }

        currentFilter = ".*"+prefix+".*";
        filteredListModel = new DefaultListModel();

        for (int i = 0; i < listModel.getSize(); i++) {
            String item = listModel.getElementAt(i).toString();

            if (item.matches(currentFilter)) {
                filteredListModel.addElement(listModel.getElementAt(i));
            }
        }

        list.setModel(filteredListModel);
    }
}
