package com.example.ftp.client.UI;

import com.example.ftp.client.ClientSocket;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginPanel extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private ClientSocket clientSocket;
    private JFrame frame;

    public LoginPanel(JFrame frame) {
        this.frame = frame;
        clientSocket = new ClientSocket("localhost", 9000); // 服务端地址和端口
        initUI();
    }

    private void initUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 0;
        add(userLabel, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0;
        add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1;
        add(passLabel, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 1;
        add(passwordField, gbc);

        loginButton = new JButton("Login");
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        add(loginButton, gbc);

        loginButton.addActionListener(this::loginAction);
    }

    private void loginAction(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Username or password cannot be empty");
            return;
        }

        try {
            clientSocket.connect();
            String resp = clientSocket.sendCommand("LOGIN|" + username + "|" + password);
            if (resp.startsWith("OK")) {
                JOptionPane.showMessageDialog(frame, "Login successful");
                // 登录成功，切换到主界面
                frame.setContentPane(new MainPanel(clientSocket));
                frame.validate();
            } else {
                JOptionPane.showMessageDialog(frame, "Login failed: " + resp);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }
}
