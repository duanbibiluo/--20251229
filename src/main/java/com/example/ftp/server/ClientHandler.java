package com.example.ftp.server;

import com.example.ftp.service.DirectoryService;
import com.example.ftp.service.FileService;
import com.example.ftp.dao.DBUtil;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private int userId;
    private int currentDirId;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                handleCommand(line);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private void handleCommand(String line) throws IOException {
        String[] parts = line.split("\\|");
        String cmd = parts[0];

        switch (cmd) {
            case "LOGIN": handleLogin(parts); break;
            case "MKDIR": handleMkdir(parts); break;
            case "LIST": handleList(); break;
            case "PUT": handlePut(parts); break;
            case "GET": handleGet(parts); break;
            default: send("ERROR|Unknown command"); break;
        }
    }

    private void handleLogin(String[] parts) throws IOException {
        String username = parts[1];
        String password = parts[2];
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id,password FROM user_info WHERE username=?")) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dbPass = rs.getString("password");
                    int uid = rs.getInt("id");
                    if (dbPass.equals(password)) {
                        this.userId = uid;
                        this.currentDirId = DirectoryService.initUserRoot(userId);
                        send("OK|LOGIN_SUCCESS");
                        return;
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        send("ERROR|LOGIN_FAILED");
    }

    private void handleMkdir(String[] parts) throws IOException {
        String dirName = parts[1];
        DirectoryService.createDirectory(userId, currentDirId, dirName);
        send("OK|MKDIR_SUCCESS");
    }

    private void handleList() throws IOException {
        String result = DirectoryService.list(currentDirId);
        send("OK|" + result);
    }

    private void handlePut(String[] parts) throws IOException {
        String filename = parts[1];
        long size = Long.parseLong(parts[2]);
        send("READY");
        FileService.receiveFile(socket.getInputStream(), userId, currentDirId, filename, size);
        send("OK|UPLOAD_SUCCESS");
    }

    private void handleGet(String[] parts) throws IOException {
        int fileId = Integer.parseInt(parts[1]);
        FileService.sendFile(socket.getOutputStream(), fileId);
    }

    private void send(String msg) throws IOException {
        writer.write(msg);
        writer.newLine();
        writer.flush();
    }
}
