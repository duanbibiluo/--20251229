package main.serve;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final FileService fileService;

    public ClientHandler(Socket socket, String baseDir) {
        this.socket = socket;
        this.fileService = new FileService(baseDir);
    }

    @Override
    public void run() {
        try {
            fileService.handleClient(socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
