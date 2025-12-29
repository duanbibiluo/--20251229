package com.example.ftp.client;

import com.example.ftp.client.UI.LoginPanel;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("FTP Client");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setContentPane(new LoginPanel(frame));
            frame.setVisible(true);
        });
    }
}
