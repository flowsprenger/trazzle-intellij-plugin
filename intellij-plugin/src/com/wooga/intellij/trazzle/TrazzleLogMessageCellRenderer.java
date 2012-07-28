package com.wooga.intellij.trazzle;

import javax.swing.*;
import java.awt.*;

public class TrazzleLogMessageCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        RemoteLogElement logMessage = (RemoteLogElement) value;
        if(logMessage.level != null)
        {
            switch(logMessage.level.charAt(0))
            {
                case 'd': // debug;
                    setForeground(new Color(-16765236));
                    break;
                case 'i': // info;
                    setForeground(new Color(-8738304));
                    break;
                case 'n': // notice;
                    setForeground(new Color(-16733644));
                    break;
                case 'w': // warning;
                    setForeground(new Color(-16733538));
                    break;
                case 'e': // error;
                    setForeground(new Color(0xFF0000));
                    break;
                case 'c': // critical;
                    setForeground(new Color(-65309));
                    break;
                case 'f': // fatal;
                    setForeground(new Color(-65432));
                    break;
                default:
                    setForeground(new Color(0x000000));
                    break;
            }
        }else{
            setForeground(new Color(0x000000));
        }
        return this;
    }
}
