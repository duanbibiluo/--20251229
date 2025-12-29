package main.serve;

import main.common.Config;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(Config.get("server.port"));
        ServerSocket serverSocket = new ServerSocket(port);

        System.out.println("Socket File Server started on port " + port);

        while (true) {
            Socket socket = serverSocket.accept(); // 阻塞
            new Thread(new ClientHandler(socket)).start();
        }
    }
}
