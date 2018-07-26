package pl.softra.swingtutorial;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

public class FileRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        Component c = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        JLabel l = (JLabel) c;
        File f = (File) value;
        l.setText(f.getName());
        l.setIcon(FileSystemView.getFileSystemView().getSystemIcon(f));
        l.setBorder(new EmptyBorder(3, 3, 3, 3));
        return l;
    }

}