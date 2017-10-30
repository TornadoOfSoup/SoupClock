package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SettingsFrame extends JFrame {

    public JCheckBox fullscreenCheckBox;
    public JCheckBox tickingSoundCheckBox;
    public JButton confirmButton;
    private JPanel settingsPanel;

    public SettingsFrame() {

        add(fullscreenCheckBox);
        add(tickingSoundCheckBox);
        add(confirmButton);

        setVisible(true);

        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
