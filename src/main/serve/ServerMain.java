package main.serve;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    public static void main(String[] args) {
        int port = 9000;
        String baseDir = "D:/ServerFiles"; // 上传文件存放的磁盘目录
        System.out.println("Server starting on port " + port);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                new Thread(new ClientHandler(clientSocket, baseDir)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
